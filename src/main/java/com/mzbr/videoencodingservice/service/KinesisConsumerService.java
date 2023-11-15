package com.mzbr.videoencodingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Semaphore;

import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.VideoEncodingDynamoTable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

@Service
@RequiredArgsConstructor
@Slf4j
public class KinesisConsumerService {
	private final KinesisAsyncClient kinesisAsyncClient;
	private final DynamoService dynamoService;
	private final EncodingService encodingService;
	private final Semaphore permits = new Semaphore(2); // 동시에 처리할 수 있는 레코드 수

	@Value("${cloud.aws.kinesis.consumer-name}")
	private String STREAM_NAME;

	private static final String JOB_TABLE = "video-encoding-table";
	private static final String JOB_ID = "id";
	private static final String STATUS = "status";

	private static final String WAITING_STATUS = "waiting";
	private static final String IN_PROGRESS_STATUS = "in_progress";
	private static final String COMPLETED_STATUS = "completed";
	private static final String FAILED_STATUS = "failed";


	@PostConstruct
	public void init() {
		String shardId = kinesisAsyncClient.listShards(ListShardsRequest.builder()
				.streamName(STREAM_NAME)
				.build())
			.join()
			.shards()
			.get(0)
			.shardId();
		String shardIterator = kinesisAsyncClient.getShardIterator(GetShardIteratorRequest.builder()
				.streamName(STREAM_NAME)
				.shardId(shardId)
				.shardIteratorType(ShardIteratorType.LATEST)
				.build())
			.join()
			.shardIterator();
		pollShard(shardIterator);
	}

	private void pollShard(String shardIterator) {
		CompletableFuture<GetRecordsResponse> getRecordsFuture = kinesisAsyncClient.getRecords(
			GetRecordsRequest.builder()
				.shardIterator(shardIterator)
				.limit(2)
				.build());
		getRecordsFuture.thenAcceptAsync(getRecordsResponse -> {
			getRecordsResponse.records().forEach(record -> {
				try {
					
					String data = StandardCharsets.UTF_8.decode(record.data().asByteBuffer()).toString();
					permits.acquire();
					updateAndProcessJob(data)
						.whenComplete((result, throwable) -> {
							permits.release();
							if (throwable != null) {
								log.error("{} 작업을 제대로 처리하지 못 했습니다.", data);
							}
						});

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});

			String nextShardIterator = getRecordsResponse.nextShardIterator();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}

			pollShard(nextShardIterator);
		});
	}

	public CompletableFuture<Void> updateAndProcessJob(String id) {
		return CompletableFuture.supplyAsync(() -> {
			VideoEncodingDynamoTable videoEncodingDynamoTable = dynamoService.getVideoEncodingDynamoTable(JOB_TABLE,
				JOB_ID, id);
			if (videoEncodingDynamoTable == null) {
				throw new CompletionException(new NoSuchElementException("조건을 만족하지 않는 작업: " + id));
			}
			if (!WAITING_STATUS.equals(videoEncodingDynamoTable.getStatus())) {

				throw new CompletionException(new NoSuchElementException("조건을 만족하지 않는 작업: " + id));
			}

			if (updateStatusToInProgressIfWaiting(id)) {
				throw new CompletionException(new NoSuchElementException("조건을 만족하지 않는 작업: " + id));
			}

			return videoEncodingDynamoTable;
		}).thenCompose(videoEncodingDynamoTable -> processJob(videoEncodingDynamoTable)).thenRun(() -> {
			updateJobStatus(id, COMPLETED_STATUS); // 상태를 완료로 변경
		}).exceptionally(e -> {
			if (e.getCause() instanceof NoSuchElementException) {
				return null;
			}
			e.printStackTrace();
			updateJobStatus(id, FAILED_STATUS);
			return null;
		});

	}

	private boolean updateStatusToInProgressIfWaiting(String id) {
		UpdateItemResponse updateItemResponse = updateJobStatus(id, IN_PROGRESS_STATUS);
		AttributeValue oldStatusValue = updateItemResponse.attributes().get(STATUS);
		return oldStatusValue == null || !WAITING_STATUS.equals(oldStatusValue.s());
	}

	private UpdateItemResponse updateJobStatus(String id, String newStatus) {
		return dynamoService.updateStatus(JOB_TABLE, JOB_ID, STATUS, id, newStatus);
	}

	private CompletableFuture<Void> processJob(VideoEncodingDynamoTable videoEncodingDynamoTable) {
		return CompletableFuture.runAsync(() -> {
			try {
				encodingService.processVideo(videoEncodingDynamoTable.getRdbId(),
					EncodeFormat.getFormatByString(videoEncodingDynamoTable.getFormat()));

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).handle((result, throwable) -> {
			if (throwable != null) {
				throw new CompletionException(throwable);
			}
			return null;
		});
	}
}

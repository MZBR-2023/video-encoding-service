package com.mzbr.videoencodingservice.service;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mzbr.videoencodingservice.model.VideoEncodingDynamoTable;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@Service
@RequiredArgsConstructor
public class DynamoService {

	private final DynamoDbClient dynamoDbClient;
	private final DynamoDbEnhancedClient dynamoDbEnhancedClient;


	@Value("${cloud.dynamo.encoding-table}")
	private String ENCODING_TABLE_NAME;

	public VideoEncodingDynamoTable getVideoEncodingDynamoTable(String tableName, String idName, String id) {
		GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
			.tableName(tableName)
			.key(Collections.singletonMap(idName, AttributeValue.builder().s(String.valueOf(id)).build()))
			.build());

		if (response == null || response.item() == null || response.item().isEmpty()) {
			throw new NoSuchElementException(id+"의 데이터는 존재하지 않습니다.");
		}

		Map<String, AttributeValue> item = response.item();

		return VideoEncodingDynamoTable.builder()
			.id(item.get("id").s()) //
			.rdbId(Long.parseLong(item.get("rdbId").n()))
			.status(item.get("status").s())
			.format(item.get("format").s())
			.build();
	}

	public UpdateItemResponse updateStatus(String tableName, String idName, String statusName, String id,
		String newStatus) {
		return dynamoDbClient.updateItem(UpdateItemRequest.builder()
			.tableName(tableName)
			.key(Collections.singletonMap(idName, AttributeValue.builder().s(String.valueOf(id)).build()))
			.updateExpression("SET #status = :newStatus")
			.expressionAttributeNames(Collections.singletonMap("#status", statusName))
			.expressionAttributeValues(
				Collections.singletonMap(":newStatus", AttributeValue.builder().s(newStatus).build()))
			.build());
	}
}

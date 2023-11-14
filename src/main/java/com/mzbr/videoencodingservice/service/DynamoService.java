package com.mzbr.videoencodingservice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.VideoEncodingDynamoTable;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@Service
@RequiredArgsConstructor
public class DynamoService {

	private final DynamoDbClient dynamoDbClient;
	private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
	private final M3U8Service m3U8Service;

	@Value("${cloud.dynamo.encoding-table}")
	private String ENCODING_TABLE_NAME;

	@Value("${cloud.dynamo.status-table}")
	private String STATUS_TABLE_NAME;

	public VideoEncodingDynamoTable getVideoEncodingDynamoTable(String tableName, String idName, String id) {
		GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
			.tableName(tableName)
			.key(Collections.singletonMap(idName, AttributeValue.builder().s(String.valueOf(id)).build()))
			.build());

		if (response == null || response.item() == null || response.item().isEmpty()) {
			throw new NoSuchElementException(id + "의 데이터는 존재하지 않습니다.");
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
		Map<String, AttributeValue> key = Collections.singletonMap(idName, AttributeValue.builder().s(id).build());
		Map<String, AttributeValueUpdate> updates = Collections.singletonMap(
			statusName,
			AttributeValueUpdate.builder()
				.value(AttributeValue.builder().s(newStatus).build())
				.action(AttributeAction.PUT)
				.build());

		UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
			.tableName(tableName)
			.key(key)
			.attributeUpdates(updates)
			.returnValues(ReturnValue.ALL_OLD.toString())
			.build();
		return dynamoDbClient.updateItem(updateItemRequest);
	}

	public void updateEncodingStatusToTrue(Long id, EncodeFormat format, int index) throws Exception {
		String attributeName = getStatusAttributeName(format);

		String updateExpression = String.format("SET %s[%d] = :status", attributeName, index);

		Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
		expressionAttributeValues.put(":status", AttributeValue.builder().bool(true).build());

		UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
			.tableName(STATUS_TABLE_NAME)
			.key(Map.of("id", AttributeValue.builder().n(String.valueOf(id)).build()))
			.updateExpression(updateExpression)
			.expressionAttributeValues(expressionAttributeValues)
			.returnValues(ReturnValue.ALL_NEW)
			.build();

		UpdateItemResponse updateItemResponse = dynamoDbClient.updateItem(updateItemRequest);
		Map<String, AttributeValue> updatedAttributes = updateItemResponse.attributes();
		checkUpdateAll(id, updatedAttributes, format);
	}

	private void checkUpdateAll(Long id, Map<String, AttributeValue> updatedAttributes, EncodeFormat encodeFormat) throws
		Exception {
		System.out.println(updatedAttributes);
		List<AttributeValue> resStatuses = updatedAttributes.get(encodeFormat.getStatusName()).l();
		boolean isResolutionComplete = resStatuses.stream().allMatch(AttributeValue::bool);
		if (!isResolutionComplete) {
			return;
		}
		List<EncodeFormat> encodeFormats = new ArrayList<>();
		for (EncodeFormat value : EncodeFormat.values()) {
			resStatuses = updatedAttributes.get(value.getStatusName()).l();
			isResolutionComplete = resStatuses.stream().allMatch(AttributeValue::bool);
			if (isResolutionComplete) {
				encodeFormats.add(value);
			}
		}

		m3U8Service.updateMasterM3u8(id, encodeFormats);

	}

	private String getStatusAttributeName(EncodeFormat format) {
		switch (format) {
			case P144:
				return "p144Status";
			case P360:
				return "p360Status";
			case P480:
				return "p480Status";
			case P720:
				return "p720Status";
			default:
				throw new IllegalArgumentException("Unknown format: " + format);
		}
	}
}

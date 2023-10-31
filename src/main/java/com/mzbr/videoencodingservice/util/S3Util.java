package com.mzbr.videoencodingservice.util;

import java.nio.file.Path;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@RequiredArgsConstructor
public class S3Util {
	private final S3Client s3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String BUCKET_NAME;
	private final S3Presigner s3Presigner;

	public void uploadFile(String filepath, String fileUploadPath) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(BUCKET_NAME)
			.key(fileUploadPath)
			.build();
		s3Client.putObject(putObjectRequest, RequestBody.fromFile(Path.of(filepath).toFile()));
	}

	public String getPresigndUrl(String url) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(BUCKET_NAME)
			.key(url)
			.build();

		GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(5))
			.getObjectRequest(getObjectRequest)
			.build();

		PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
		return presignedGetObjectRequest.url().toString();
	}
}

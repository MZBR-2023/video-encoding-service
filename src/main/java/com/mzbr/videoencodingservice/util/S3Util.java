package com.mzbr.videoencodingservice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
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

	@Value("${cloud.aws.s3.url}")
	private String S3_URL;

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
	public Path getFileToLocalDirectory(String fileName) throws IOException {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(BUCKET_NAME)
			.key(fileName)
			.build();
		ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
		byte[] data = objectBytes.asByteArray();

		String extension = "";
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i);
		}

		File myFile = new File(UUID.randomUUID()+ extension);
		OutputStream os = new FileOutputStream(myFile);
		os.write(data);
		os.close();
		return myFile.getAbsoluteFile().toPath();
	}

	public String fileUrl(String fileName){
		return S3_URL + fileName;
	}
}

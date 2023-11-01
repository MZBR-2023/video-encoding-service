package com.mzbr.videoencodingservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.EncodedVideoSegment;
import com.mzbr.videoencodingservice.model.VideoSegment;
import com.mzbr.videoencodingservice.repository.EncodedVideoSegmentRepository;
import com.mzbr.videoencodingservice.repository.VideoSegmentRepository;
import com.mzbr.videoencodingservice.util.S3Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncodingServiceImpl implements EncodingService {
	private final VideoSegmentRepository videoSegmentRepository;
	private final EncodedVideoSegmentRepository encodedVideoSegmentRepository;
	private final S3Util s3Util;
	private static final String CURRENT_WORKING_DIR = System.getProperty("user.dir");

	@Value("${folder.prefix}")
	String FOLDER_PREFIX;

	@Override
	public void processVideo(Long videoSegmentId, EncodeFormat encodeFormat) throws Exception {

		//id로 비디오 불러오기
		VideoSegment videoSegment = videoSegmentRepository.findById(videoSegmentId).orElseThrow();
		String fileName = generateFileName(videoSegment, encodeFormat);
		String filePath = CURRENT_WORKING_DIR + "/" + fileName;

		try {
			//FFmpeg을 이용한 비디오 변환
			convertVideo(encodeFormat, videoSegment, fileName);

			//S3 스토리지에 업로드 후 DB에 값 저장
			uploadAndSave(encodeFormat, videoSegment, filePath);
		} finally {
			//임시 파일 삭제
			if (Files.exists(Path.of(filePath))) {
				deleteTemporaryFile(filePath);
			}
		}
	}

	private void uploadAndSave(EncodeFormat encodeFormat, VideoSegment videoSegment, String filePath) {
		String uploadPath = generateUploadPath(videoSegment, encodeFormat);
		//s3 업로드
		uploadToS3(filePath, uploadPath);
		//DB에 저장
		createAndSaveEncodedSegment(encodeFormat, videoSegment, uploadPath);
	}

	private void convertVideo(EncodeFormat encodeFormat, VideoSegment videoSegment, String fileName) throws Exception {
		FFmpeg fFmpeg = FFmpeg.atPath();
		//input 준비
		fFmpeg.addInput(prepareVideoInput(videoSegment.getVideoUrl()));
		//비디오 출력 설정
		setVideoExport(encodeFormat, fileName, fFmpeg);
		//비디오 생성
		fFmpeg.execute();
	}

	private void createAndSaveEncodedSegment(EncodeFormat encodeFormat, VideoSegment videoSegment, String uploadPath) {
		EncodedVideoSegment encodedVideoSegment = EncodedVideoSegment.builder()
			.encodeFormat(encodeFormat)
			.videoSegment(videoSegment)
			.url(uploadPath)
			.build();
		encodedVideoSegmentRepository.save(encodedVideoSegment);
	}

	private String generateUploadPath(VideoSegment videoSegment, EncodeFormat encodeFormat) {
		StringBuilder uploadPath = new StringBuilder(FOLDER_PREFIX);
		uploadPath.append(videoSegment.getVideoName()).append("/");
		uploadPath.append(encodeFormat.name()).append("/");
		uploadPath.append(videoSegment.getVideoSequence()).append(".ts");
		return uploadPath.toString();
	}

	private void setVideoExport(EncodeFormat encodeFormat, String fileName, FFmpeg fFmpeg) throws Exception {
		fFmpeg.addOutput(UrlOutput.toPath(Path.of(fileName))
			.addArguments("-g", "60")
			.addArguments("-b:v", encodeFormat.getBitRateK() + "K")
			.addArguments("-c:v", "libx264")
			.addArguments("-vf", getScale(encodeFormat))
		);
	}

	private String generateFileName(VideoSegment videoSegment, EncodeFormat encodeFormat) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(videoSegment.getVideoName())
			.append("[")
			.append(videoSegment.getVideoSequence())
			.append("]");

		stringBuilder.append("_").append(encodeFormat.name()).append(".ts");

		return stringBuilder.toString();
	}

	@Override
	public Input prepareVideoInput(String videoOriginUrl) throws Exception {
		return UrlInput.fromUrl(String.format("\"%s\"", s3Util.getPresigndUrl(videoOriginUrl)));
	}

	@Override
	public String getScale(EncodeFormat encodeFormat) throws Exception {
		StringBuilder stringBuilder = new StringBuilder("scale=");
		stringBuilder.append(encodeFormat.getWidth()).append(":").append(encodeFormat.getHeight());

		return stringBuilder.toString();
	}

	@Override
	public void uploadToS3(String localFilePath, String uploadFilePath) {
		s3Util.uploadFile(localFilePath, uploadFilePath);
	}

	@Override
	public void deleteTemporaryFile(String localFilePath) throws IOException {
		Files.delete(Path.of(localFilePath));
	}
}

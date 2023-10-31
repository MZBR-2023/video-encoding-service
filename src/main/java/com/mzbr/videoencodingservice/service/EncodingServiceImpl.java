package com.mzbr.videoencodingservice.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.VideoSegment;
import com.mzbr.videoencodingservice.repository.VideoSegmentRepository;
import com.mzbr.videoencodingservice.util.S3Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncodingServiceImpl implements EncodingService{
	private final VideoSegmentRepository videoSegmentRepository;
	private final S3Util s3Util;

	@Value("${folder.prefix}")
	String FOLDER_PREFIX;

	@Override
	public void processVideo(Long videoSegmentId, EncodeFormat encodeFormat) throws Exception {
		//id로 비디오 불러오기
		VideoSegment videoSegment = videoSegmentRepository.findById(videoSegmentId).orElseThrow();

		String fileName = generateFileName(videoSegment, encodeFormat);

		FFmpeg fFmpeg = FFmpeg.atPath();

		//input 준비
		fFmpeg.addInput(prepareVideoInput(videoSegment.getVideoUrl()));

		//비디오 출력 설정
		setVideoExport(encodeFormat, fileName, fFmpeg);

		//비디오 생성
		fFmpeg.execute();


		String filePath = getFileAbsolutePath(fileName);
		String uploadPath = generateUploadPath(videoSegment,encodeFormat);

		//s3 업로드
		uploadToS3(filePath, uploadPath);

		//임시 파일 삭제

	}

	private String generateUploadPath(VideoSegment videoSegment, EncodeFormat encodeFormat) {
		StringBuilder stringBuilder = new StringBuilder(FOLDER_PREFIX);
		stringBuilder.append(videoSegment.getVideoName()).append("/");
		stringBuilder.append(encodeFormat.name()).append("/");
		stringBuilder.append(videoSegment.getVideoSequence()).append(".ts");
		return stringBuilder.toString();
	}

	private void setVideoExport(EncodeFormat encodeFormat, String fileName, FFmpeg fFmpeg) throws Exception {
		fFmpeg.addOutput(UrlOutput.toPath(Path.of(fileName))
			.addArguments("-g", "60")
			.addArguments("-b:v", String.format("%dK", encodeFormat.getBitRateK()))
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
		return  UrlInput.fromUrl(String.format("\"%s\"", s3Util.getPresigndUrl(videoOriginUrl)));
	}

	@Override
	public String getScale(EncodeFormat encodeFormat) throws Exception {
		StringBuilder stringBuilder = new StringBuilder("scale=");
		stringBuilder.append(encodeFormat.getWidth()).append(":").append(encodeFormat.getHeight());

		return stringBuilder.toString();
	}

	@Override
	public String getFileAbsolutePath(String videoName) throws Exception {
		String projectRootPath = System.getProperty("user.dir");
		Path directory = Paths.get(projectRootPath);

		Path filePath = directory.resolve(videoName);
		if(Files.exists(filePath)){
			return filePath.toFile().getAbsolutePath();
		}
		throw new IllegalArgumentException("파일 검색 불가");
	}

	@Override
	public void uploadToS3(String localFilePath, String uploadFilePath) {
		s3Util.uploadFile(localFilePath,uploadFilePath);
	}

	@Override
	public void deleteTemporaryFile(String localFilePath) {

	}
}

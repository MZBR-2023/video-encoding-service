package com.mzbr.videoencodingservice.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.VideoSegment;
import com.mzbr.videoencodingservice.repository.VideoSegmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncodingServiceImpl implements EncodingService{
	private final VideoSegmentRepository videoSegmentRepository;

	@Override
	public void processVideo(Long videoSegmentId, EncodeFormat encodeFormat) throws Exception {
		//id로 비디오 불러오기
		VideoSegment videoSegment = videoSegmentRepository.findById(videoSegmentId).orElseThrow();

		String fileName = generateFileName(videoSegment, encodeFormat);

		FFmpeg fFmpeg = FFmpeg.atPath();

		//input 준비
		fFmpeg.addInput(prepareVideoInput(videoSegment.getVideoUrl()));

		//비디오 출력 설정

		//비디오 생성
		fFmpeg.execute();


		getFileAbsolutePath(fileName);

		//s3 업로드

		//임시 파일 삭제

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
		return null;
	}

	@Override
	public String getScale(EncodeFormat encodeFormat) throws Exception {
		return null;
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

	}

	@Override
	public void deleteTemporaryFile(String localFilePath) {

	}
}

package com.mzbr.videoencodingservice.service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.mzbr.videoencodingservice.enums.EncodeFormat;

public interface EncodingService {
	void processVideo(Long videoSegmentId, EncodeFormat encodeFormat) throws Exception;
	Input prepareVideoInput(String videoOriginUrl) throws Exception;
	String getScale(EncodeFormat encodeFormat) throws Exception;

	String getFileAbsolutePath(String videoName) throws Exception;

	void uploadToS3(String localFilePath, String uploadFilePath);
	void deleteTemporaryFile(String localFilePath);
}

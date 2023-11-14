package com.mzbr.videoencodingservice.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.VideoData;
import com.mzbr.videoencodingservice.repository.VideoDataRepository;
import com.mzbr.videoencodingservice.util.S3Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class M3U8ServiceImpl implements M3U8Service {
	private final S3Util s3Util;
	private final VideoDataRepository videoDataRepository;

	@Override
	public void updateMasterM3u8(VideoData videoData, EncodeFormat encodeFormat, String masterUrl) throws Exception {
		// m3u8 파일을 만듭니다.
		Path newM3u8Path = null;

		try {
			newM3u8Path = generateM3U8Content(videoData, encodeFormat);
			// s3에 업로드합니다.
			s3Util.uploadFile(newM3u8Path.toAbsolutePath().toString(), masterUrl);

			// DB에 masterUrl을 업데이트합니다.
			if (videoData.getMasterUrl() == null) {
				videoData.updateMasterUrl(masterUrl);
				videoDataRepository.save(videoData);
			}

		} finally {
			try {
				Files.deleteIfExists(newM3u8Path);
			} catch (IOException e) {

			}

		}

	}

	private Path generateM3U8Content(VideoData videoData, EncodeFormat encodeFormat) throws IOException {
		Path m3u8FilePath;
		List<String> lines = new ArrayList<>();

		// masterUrl이 있으면 s3에서 받아옵니다.
		if (videoData.getMasterUrl() != null) {
			m3u8FilePath = s3Util.getFileToLocalDirectory(videoData.getMasterUrl());
			lines = Files.readAllLines(m3u8FilePath);
		} else {
			// 없으면 새로 작성합니다.
			m3u8FilePath = Files.createTempFile("master", ".m3u8");
		}

		// 새 해상도의 플레이리스트 엔트리를 추가합니다.
		String newEntry = String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d%n%s",
			encodeFormat.getBandwidth(),
			encodeFormat.getWidth(),
			encodeFormat.getHeight(),
			videoData.getUrlByEncodeFormat(encodeFormat)
		);
		lines.add(newEntry);

		// 파일에 쓰기
		Files.write(m3u8FilePath, lines, StandardCharsets.UTF_8);

		// 파일의 경로를 반환합니다.
		return m3u8FilePath;
	}

}

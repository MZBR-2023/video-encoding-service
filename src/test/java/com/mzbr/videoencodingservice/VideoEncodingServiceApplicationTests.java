package com.mzbr.videoencodingservice;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.VideoSegment;
import com.mzbr.videoencodingservice.repository.VideoSegmentRepository;
import com.mzbr.videoencodingservice.service.EncodingService;

@SpringBootTest
@ActiveProfiles("ssafy")
@Profile("ssafy")
class VideoEncodingServiceApplicationTests {

	@Autowired
	 private EncodingService encodingService;

	@Autowired
	private VideoSegmentRepository videoSegmentRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void 인코딩_360P_테스트() throws Exception {
		VideoSegment videoSegment =
			VideoSegment.builder()
				.id(1L)
				.videoName("2540a249-fd82-4ab1-ad15-620c5eb0f807")
				.videoUrl("video/2540a249-fd82-4ab1-ad15-620c5eb0f807/2540a249-fd82-4ab1-ad15-620c5eb0f807[000].mov")
				.videoSequence(0)
				.build();
		videoSegmentRepository.save(videoSegment);

		encodingService.processVideo(1L, EncodeFormat.P360);
	}
	
	@Test
	@Transactional
	void 리스트_360P() throws Exception {
		List<VideoSegment> videoSegmentList = new ArrayList<>();
		for (int i = 0; i <= 23; i++) {
			VideoSegment videoSegment =
				VideoSegment.builder()
					.id(Long.valueOf(i+1))
					.videoName("2540a249-fd82-4ab1-ad15-620c5eb0f807")
					.videoUrl(String.format(
						"video/2540a249-fd82-4ab1-ad15-620c5eb0f807/2540a249-fd82-4ab1-ad15-620c5eb0f807[%03d].mov", i))
					.videoSequence(i)
					.build();
			videoSegmentList.add(videoSegment);
		}
		videoSegmentRepository.saveAll(videoSegmentList);


		for (Long i = 1L; i <= 24; i++) {
			System.out.println(i+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			encodingService.processVideo(i, EncodeFormat.P360);
		}

	}

}

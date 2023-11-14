package com.mzbr.videoencodingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.EncodedVideoSegment;
import com.mzbr.videoencodingservice.model.Video;

@Repository
public interface EncodedVideoSegmentRepository extends JpaRepository<EncodedVideoSegment, Long> {
	Integer countByVideoAndEncodeFormat(Video video, EncodeFormat encodeFormat);
}

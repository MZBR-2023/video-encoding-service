package com.mzbr.videoencodingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoencodingservice.model.VideoSegment;

@Repository
public interface VideoSegmentRepository extends JpaRepository<VideoSegment, Long> {
}

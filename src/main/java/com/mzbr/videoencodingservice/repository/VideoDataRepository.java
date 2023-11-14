package com.mzbr.videoencodingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoencodingservice.model.VideoData;

@Repository
public interface VideoDataRepository extends JpaRepository<VideoData, Long> {
}

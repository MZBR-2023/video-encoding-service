package com.mzbr.videoencodingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoencodingservice.model.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}

package com.mzbr.videoencodingservice.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "videoSegment")
@ToString
public class VideoSegment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	String videoUrl;
	String videoName;
	Integer videoSequence;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "video_id")
	Video video;
}

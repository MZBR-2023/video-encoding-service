package com.mzbr.videoencodingservice.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.mzbr.videoencodingservice.enums.EncodeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "encoded_video_segment")
public class EncodedVideoSegment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	String url;

	@Enumerated(EnumType.STRING)
	EncodeFormat encodeFormat;

	@ManyToOne
	@JoinColumn(name = "video_id")
	Video video;
}

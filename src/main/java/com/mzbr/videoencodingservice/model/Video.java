package com.mzbr.videoencodingservice.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "video")
public class Video {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	Integer segmentCount;

	@OneToOne(mappedBy = "videoEntity", fetch = FetchType.LAZY)
	VideoData videoData;
}

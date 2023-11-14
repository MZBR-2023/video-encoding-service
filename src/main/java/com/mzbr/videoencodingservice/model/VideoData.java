package com.mzbr.videoencodingservice.model;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.mzbr.videoencodingservice.enums.EncodeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "video_data")
public class VideoData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String masterUrl;
	String P144Url;
	String P360Url;
	String P480Url;
	String P720Url;



	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "video_id")
	Video videoEntity;

	public void updateMasterUrl(String masterUrl) {
		this.masterUrl = masterUrl;
	}

	public String getUrlByEncodeFormat(EncodeFormat encodeFormat) {
		switch (encodeFormat) {
			case P144:
				return getP144Url();
			case P360:
				return getP360Url();
			case P480:
				return getP480Url();
			case P720:
				return getP720Url();
			default:
				return null;
		}
	}

}

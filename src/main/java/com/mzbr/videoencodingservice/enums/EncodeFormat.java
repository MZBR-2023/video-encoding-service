package com.mzbr.videoencodingservice.enums;

import lombok.Getter;

@Getter
public enum EncodeFormat {
	P720(720, 1280, 2000, 2800000, "p720Status"),
	P480(480, 854, 700, 1400000, "p480Status"),
	P360(360, 640, 500, 800000, "p360Status"),
	P144(144, 256, 150, 500000, "p144Status");

	int width;
	int height;
	int bitRateK;
	int bandwidth;
	String statusName;

	EncodeFormat(int width, int height, int bitRateK, int bandwidth, String statusName) {
		this.width = width;
		this.height = height;
		this.bitRateK = bitRateK;
		this.bandwidth = bandwidth;
		this.statusName = statusName;
	}

	public static EncodeFormat getFormatByString(String formatString) {
		for (EncodeFormat value : EncodeFormat.values()) {
			if (formatString.equals(value.name())) {
				return value;
			}
		}
		throw new IllegalArgumentException("잘못된 formatString");
	}
}

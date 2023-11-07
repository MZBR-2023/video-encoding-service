package com.mzbr.videoencodingservice.enums;

import lombok.Getter;

@Getter
public enum EncodeFormat {
	P720(720, 1280, 2000),
	P480(480, 854, 700),
	P360(360, 640, 500),
	P144(144,256,150);

	int width;
	int height;
	int bitRateK;

	EncodeFormat(int width, int height, int bitRateK) {
		this.width = width;
		this.height = height;
		this.bitRateK = bitRateK;
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

package com.mzbr.videoencodingservice.enums;

import lombok.Getter;

@Getter
public enum EncodeFormat {
	P720(720, 1280, 2000, 2800000),
	P480(480, 854, 700, 1400000),
	P360(360, 640, 500, 800000),
	P144(144, 256, 150, 500000);

	int width;
	int height;
	int bitRateK;
	int bandwidth;

	EncodeFormat(int width, int height, int bitRateK, int bandwidth) {
		this.width = width;
		this.height = height;
		this.bitRateK = bitRateK;
		this.bandwidth = bandwidth;
	}
}

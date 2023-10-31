package com.mzbr.videoencodingservice.enums;

import lombok.Getter;

@Getter
public enum EncodeFormat {
	P720(720, 1280, 2000),
	P480(480, 854, 700),
	P360(360, 640, 500);

	int width;
	int height;
	int bitRate;

	EncodeFormat(int width, int height, int bitRate) {
		this.width = width;
		this.height = height;
		this.bitRate = bitRate;
	}
}

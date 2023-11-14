package com.mzbr.videoencodingservice.service;

import com.mzbr.videoencodingservice.enums.EncodeFormat;
import com.mzbr.videoencodingservice.model.VideoData;

public interface M3U8Service {
	void updateMasterM3u8(VideoData videoData, EncodeFormat encodeFormat, String masterUrl)throws Exception;
}

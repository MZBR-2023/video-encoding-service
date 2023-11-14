package com.mzbr.videoencodingservice.service;

import java.util.List;

import com.mzbr.videoencodingservice.enums.EncodeFormat;

public interface M3U8Service {
	void updateMasterM3u8(Long videoId, List<EncodeFormat> encodeFormats)throws Exception;
}

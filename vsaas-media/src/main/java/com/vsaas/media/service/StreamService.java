package com.vsaas.media.service;

import com.vsaas.media.config.SrsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StreamService {

    private final SrsProperties srsProperties;

    public Map<String, String> buildPlayUrls(String streamKey) {
        String app = srsProperties.getApp();
        String httpHost = srsProperties.getHttpHost().replaceAll("/$", "");
        Map<String, String> urls = new HashMap<>();
        urls.put("flv", httpHost + "/" + app + "/" + streamKey + ".flv");
        urls.put("hls", httpHost + "/" + app + "/" + streamKey + ".m3u8");
        urls.put("rtmp", "rtmp://" + httpHost.replace("http://", "").replace("https://", "") + "/" + app + "/" + streamKey);
        if (srsProperties.isWebrtcEnabled()) {
            urls.put("webrtc", httpHost + "/rtc/v1/whep/?app=" + app + "&stream=" + streamKey);
        }
        return urls;
    }

    public String buildPublishUrl(String streamKey) {
        String host = srsProperties.getHttpHost()
                .replace("http://", "")
                .replace("https://", "")
                .replaceAll("/$", "");
        return "rtmp://" + host + "/" + srsProperties.getApp() + "/" + streamKey;
    }
}

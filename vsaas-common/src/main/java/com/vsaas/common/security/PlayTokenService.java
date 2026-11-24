package com.vsaas.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class PlayTokenService {

    @Value("${vsaas.play.secret:vsaas-play-token-secret-change-me}")
    private String playSecret;

    @Value("${vsaas.play.expire-seconds:3600}")
    private long expireSeconds;

    public String sign(String streamKey) {
        long exp = System.currentTimeMillis() / 1000 + expireSeconds;
        String payload = streamKey + "." + exp;
        return payload + "." + hmac(payload);
    }

    public boolean verify(String token) {
        if (token == null) return false;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;
        try {
            long expTs = Long.parseLong(parts[1]);
            if (System.currentTimeMillis() / 1000 > expTs) return false;
            return hmac(parts[0] + "." + parts[1]).equals(parts[2]);
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, String> appendToken(Map<String, String> urls, String streamKey) {
        String token = sign(streamKey);
        Map<String, String> out = new HashMap<>(urls);
        out.put("token", token);
        out.put("expireSeconds", String.valueOf(expireSeconds));
        for (String k : new String[]{"flv", "hls", "webrtc"}) {
            String u = out.get(k);
            if (u != null) {
                out.put(k, u + (u.contains("?") ? "&" : "?") + "token=" + token);
            }
        }
        return out;
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(playSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

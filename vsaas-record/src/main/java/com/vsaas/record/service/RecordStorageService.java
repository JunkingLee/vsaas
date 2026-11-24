package com.vsaas.record.service;

import com.vsaas.common.core.BusinessException;
import com.vsaas.record.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RecordStorageService {

    private final MinioClient minioClient;
    private final MinioProperties props;

    public String buildObjectKey(Long tenantId, Long channelId, LocalDateTime startTime, int seq) {
        DateTimeFormatter day = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return String.format("%d/%d/%s/%s_%04d.mp4",
                tenantId, channelId, startTime.format(day), startTime.format(ts), seq);
    }

    public String buildAccessUrl(String objectKey) {
        return props.getEndpoint().replaceAll("/$", "") + "/" + props.getBucket() + "/" + objectKey;
    }

    public String getBucket() {
        return props.getBucket();
    }

    public void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(props.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(props.getBucket()).build());
            }
        } catch (Exception e) {
            throw new BusinessException(500, "MinIO bucket 检查失败: " + e.getMessage());
        }
    }

    public String upload(String objectKey, MultipartFile file) {
        ensureBucket();
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType() != null ? file.getContentType() : "video/mp4")
                    .build());
            return buildAccessUrl(objectKey);
        } catch (Exception e) {
            throw new BusinessException(500, "上传失败: " + e.getMessage());
        }
    }

    public String presignGetUrl(String objectKey, int expireSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .expiry(expireSeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new BusinessException(500, "生成预签名URL失败: " + e.getMessage());
        }
    }

    public String presignPutUrl(String objectKey, int expireSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .expiry(expireSeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new BusinessException(500, "生成预签名上传URL失败: " + e.getMessage());
        }
    }

    public Map<String, String> buildPathInfo(Long tenantId, Long channelId, LocalDateTime startTime, int seq) {
        String key = buildObjectKey(tenantId, channelId, startTime, seq);
        Map<String, String> data = new HashMap<>();
        data.put("objectKey", key);
        data.put("bucket", props.getBucket());
        data.put("accessUrl", buildAccessUrl(key));
        try {
            data.put("presignPutUrl", presignPutUrl(key, 3600));
            data.put("presignGetUrl", presignGetUrl(key, 3600));
        } catch (Exception ignored) {
        }
        return data;
    }

    public String uploadStream(String objectKey, InputStream in, long size, String contentType) {
        ensureBucket();
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .stream(in, size, -1)
                    .contentType(contentType != null ? contentType : "video/mp4")
                    .build());
            return buildAccessUrl(objectKey);
        } catch (Exception e) {
            throw new BusinessException(500, "上传失败: " + e.getMessage());
        }
    }

    public void deleteObject(String filePathOrKey) {
        if (filePathOrKey == null || filePathOrKey.isEmpty()) return;
        String key = filePathOrKey;
        String marker = "/" + props.getBucket() + "/";
        if (key.contains(marker)) {
            key = key.substring(key.indexOf(marker) + marker.length());
        } else if (key.startsWith("http")) {
            int idx = key.indexOf("/", key.indexOf("://") + 3);
            if (idx > 0) key = key.substring(idx + 1);
            if (key.startsWith(props.getBucket() + "/")) {
                key = key.substring(props.getBucket().length() + 1);
            }
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(key)
                    .build());
        } catch (Exception e) {
        }
    }
}

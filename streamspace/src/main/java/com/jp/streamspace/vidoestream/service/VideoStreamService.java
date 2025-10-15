package com.jp.streamspace.vidoestream.service;

import com.jp.streamspace.vidoestream.mapper.VideoMapper;
import com.jp.streamspace.vidoestream.modal.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;
import java.util.Optional;

@Service
public class VideoStreamService {

    private static final Logger log = LoggerFactory.getLogger(VideoStreamService.class);

    private final VideoMapper videoMapper;
    private final S3Presigner s3Presigner;

    @Value("${wasabi.bucket}")
    private String bucketName;

    public VideoStreamService(VideoMapper videoMapper, S3Presigner s3Presigner) {
        this.videoMapper = videoMapper;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Generates a presigned URL for streaming a video
     */
    public Optional<String> generatePresignedUrl(Integer videoId) {
        log.info("🔍 Fetching video metadata for id={}", videoId);
        Video video = videoMapper.findById(videoId);

        if (video == null) {
            log.error("❌ Video not found for id={}", videoId);
            return Optional.empty();
        }

        String masterKey = video.getMasterKey();
        if (masterKey == null || masterKey.isEmpty()) {
            log.error("❌ Master key missing for video id={}", videoId);
            return Optional.empty();
        }

        // Strip "videos/" prefix if present
        String objectKey = masterKey.startsWith("videos/") ? masterKey.substring("videos/".length()) : masterKey;
        log.info("🎯 Using object key for presigned URL: {}", objectKey);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1)) // 1 hour validity
                    .getObjectRequest(getObjectRequest)
                    .build();

            String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();
            log.info("✅ Successfully generated presigned URL for videoId={}", videoId);

            return Optional.of(presignedUrl);
        } catch (Exception e) {
            log.error("❌ Error generating presigned URL for videoId={}: {}", videoId, e.getMessage(), e);
            return Optional.empty();
        }
    }
}

package com.jp.streamspace.vidoestream.service;

import com.jp.streamspace.vidoestream.config.RabbitConfig;
import com.jp.streamspace.vidoestream.dto.NotifyUploadRequest;
import com.jp.streamspace.vidoestream.dto.PresignRequest;
import com.jp.streamspace.vidoestream.mapper.VideoMapper;
import com.jp.streamspace.vidoestream.modal.Video;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    private final VideoMapper videoMapper;
    private final StorageService storageService;
    private final RabbitTemplate rabbitTemplate;

    public VideoService(VideoMapper videoMapper, StorageService storageService, RabbitTemplate rabbitTemplate) {
        this.videoMapper = videoMapper;
        this.storageService = storageService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Create DB record and return presigned url + s3 key.
     * Postgres auto-increments the ID, so we insert first, then generate S3 key.
     */
    public PresignResult createPresignedUpload(PresignRequest req) {
        log.info("Received presign request for file='{}', uploadedBy='{}'", req.getFilename(), req.getUploadedBy());

        Video v = Video.builder()
                .uploadedBy(req.getUploadedBy())
                .durationSeconds(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPrivate(req.getIsPrivate() != null ? req.getIsPrivate() : Boolean.FALSE)
                .title(req.getTitle())
                .description(req.getDescription())
                .status("uploading")   // initial state
                .masterKey(null)
                .thumbnailUrl(null)
                .build();

        log.debug("Inserting new video record into database: {}", v);
        videoMapper.insert(v);
        log.info("Video record created with ID={}", v.getId());

        // Generate S3 key and presigned URL
        String s3Key = String.format("videos/%d/%s", v.getId(), sanitizeFilename(req.getFilename()));
        String presignedUrl = storageService.presignPut(s3Key, 15 * 60);

        log.info("Generated presigned upload URL for videoId={}, s3Key='{}'", v.getId(), s3Key);

        return new PresignResult(v.getId(), presignedUrl, s3Key);
    }

    /**
     * Called by client after upload completes.
     * If object exists -> update DB and publish job.
     * If object not found -> return false.
     */
    public boolean handleNotifyUpload(NotifyUploadRequest req) {
        log.info("Received notify upload request for videoId={}, s3Key='{}'", req.getVideoId(), req.getS3Key());

        int id = req.getVideoId();
        String s3Key = req.getS3Key();

        // Fetch from DB
        Video video = videoMapper.findById(id);
        if (video == null) {
            log.warn("Video with ID={} not found in database.", id);
            return false;
        }

        // Verify object exists in storage
        log.debug("Checking if uploaded object exists in storage for key='{}'", s3Key);
        if (!storageService.doesObjectExist(s3Key)) {
            log.error("Object not found in storage for s3Key='{}'", s3Key);
            return false;
        }

        // ✅ Store only the object key in DB
        log.info("Updating videoId={} master_key='{}'", id, s3Key);
        videoMapper.updateMasterKey(id, s3Key);

        // ✅ Update video status
        log.info("Updating videoId={} status to 'uploaded'", id);
        videoMapper.updateStatus(id, "uploaded");

        // ✅ Build full URL dynamically (for FFmpeg or queue payload)
        String masterUrl = storageService.objectUrl(s3Key);

        // Prepare payload for transcoding job
        // Prepare payload for transcoding job including s3Key
        String payload = String.format(
                "{\"videoId\":%d,\"s3Key\":\"%s\",\"inputUrl\":\"%s\",\"outputPrefix\":\"videos/%d\"}",
                id, s3Key, masterUrl, id
        );
        log.info("payload : " + payload);
        log.info("Publishing transcoding job for videoId={} to queue='{}'", id, RabbitConfig.TRANSCODE_QUEUE);
        rabbitTemplate.convertAndSend(RabbitConfig.TRANSCODE_QUEUE, payload);

        log.info("Notify upload completed successfully for videoId={}", id);
        return true;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "upload.mp4";
        String safeName = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        log.debug("Sanitized filename '{}' to '{}'", filename, safeName);
        return safeName;
    }

    public static class PresignResult {
        public final int videoId;
        public final String url;
        public final String s3Key;

        public PresignResult(int videoId, String url, String s3Key) {
            this.videoId = videoId;
            this.url = url;
            this.s3Key = s3Key;
        }

        @Override
        public String toString() {
            return "PresignResult{" +
                    "videoId=" + videoId +
                    ", url='" + url + '\'' +
                    ", s3Key='" + s3Key + '\'' +
                    '}';
        }
    }
}

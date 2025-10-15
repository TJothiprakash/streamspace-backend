package com.jp.streamspace.vidoestream.controller;

import com.jp.streamspace.vidoestream.dto.NotifyUploadRequest;
import com.jp.streamspace.vidoestream.dto.PresignRequest;
import com.jp.streamspace.vidoestream.dto.PresignResponse;
import com.jp.streamspace.vidoestream.service.VideoProducer;
import com.jp.streamspace.vidoestream.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);


    private final VideoService videoService;
    private final VideoProducer producer;

    public VideoController(VideoService videoService, VideoProducer producer) {
        this.videoService = videoService;
        this.producer = producer;
    }

    /**
     * Request a presigned URL to upload the original file to Wasabi.
     * The server creates a DB record (status=uploading) and returns the presigned URL + videoId + s3Key.
     */
    @PostMapping("/presign")
    public ResponseEntity<PresignResponse> presign(@RequestBody PresignRequest request) {
        logger.info("Received presign request for file: {} (uploadedBy: {})",
                request.getFilename(), request.getUploadedBy());

        var user = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Authenticated user: {}", user != null ? user.getName() : "Anonymous");

        VideoService.PresignResult result = videoService.createPresignedUpload(request);

        logger.info("Generated presigned URL for videoId={} and s3Key={}", result.videoId, result.s3Key);
        logger.info("result.url : " + result.url);
        PresignResponse response = new PresignResponse(result.videoId, result.url, result.s3Key);
        return ResponseEntity.ok(response);
    }

    /**
     * Client calls this after upload completes.
     * Request body: { videoId: <uuid>, s3Key: "<videos/{uuid}/file.mp4>" }
     * If object exists -> update DB to uploaded and enqueue job. Returns 200.
     * If object not found -> return 400 (client should retry).
     */
    @PostMapping("/notify-upload")
    public ResponseEntity<?> notifyUpload(@RequestBody NotifyUploadRequest req) {
        logger.info("Received notify-upload for videoId={} with s3Key={}", req.getVideoId(), req.getS3Key());

        boolean verified = videoService.handleNotifyUpload(req);

        if (verified) {
            logger.info("Upload verification successful for videoId={}", req.getVideoId());
            return ResponseEntity.ok().body("verified");
        } else {
            logger.warn("Upload verification FAILED for videoId={} (object not found or invalid)", req.getVideoId());
            return ResponseEntity.badRequest().body("object-not-found-or-invalid");
        }
    }

    /**
     * Debug endpoint to inspect current authentication context.
     */
    @GetMapping("/debug-auth")
    public String debugAuth() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.debug("Debug-auth called by user={} with roles={}", auth.getName(), auth.getAuthorities());
            return auth.getName() + " / " + auth.getAuthorities();
        } else {
            logger.debug("Debug-auth called with no active authentication.");
            return "No auth";
        }
    }

    @GetMapping("/send-job")
    public String sendJob(
            @RequestParam int videoId,
            @RequestParam String inputUrl,
            @RequestParam String outputPrefix) {

        logger.info("Received request to send job: videoId={}, inputUrl={}, outputPrefix={}",
                videoId, inputUrl, outputPrefix);

        try {
            producer.sendTranscodeJob(videoId, inputUrl, outputPrefix);
            logger.info("✅ Job successfully sent to queue for videoId={}", videoId);
            return "✅ Job sent for videoId=" + videoId;
        } catch (Exception e) {
            logger.error("❌ Failed to send job for videoId={}", videoId, e);
            return "❌ Failed to send job for videoId=" + videoId;
        }
    }
    @GetMapping("/user")
    public ResponseEntity<?> getUserVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var result = videoService.getUserVideos(username, page, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<?> getAllVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        var result = videoService.getAllPublicVideos(page, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/private")
    public ResponseEntity<?> getPrivateVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        logger.info("Fetching private videos for user: {}", username);

        var result = videoService.getPrivateVideos(username, page, limit);
        return ResponseEntity.ok(result);
    }

}

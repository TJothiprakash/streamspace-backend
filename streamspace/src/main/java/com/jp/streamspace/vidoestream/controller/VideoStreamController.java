package com.jp.streamspace.vidoestream.controller;

import com.jp.streamspace.vidoestream.service.VideoStreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
public class VideoStreamController {

    private static final Logger log = LoggerFactory.getLogger(VideoStreamController.class);

    private final VideoStreamService videoStreamService;

    public VideoStreamController(VideoStreamService videoStreamService) {
        this.videoStreamService = videoStreamService;
    }

    /**
     * Handles client requests for video streaming.
     * Example: GET /api/videos/stream/17
     */
    @GetMapping("/stream/{id}")
    public ResponseEntity<?> streamVideo(@PathVariable("id") Integer videoId) {
        log.info("🎬 Received request to stream video with id={}", videoId);

        return videoStreamService.generatePresignedUrl(videoId)
                .<ResponseEntity<?>>map(url -> {
                    log.info("📡 Returning presigned URL for videoId={}", videoId);
                    return ResponseEntity.ok().body(url);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Failed to find or generate URL for videoId={}", videoId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Video not found or presigned URL generation failed");
                });
    }
    /**
     * Handles client requests for video streaming.
     * Example: GET /api/videos/stream/17
     */
    @GetMapping("mp4/stream/{id}")
    public ResponseEntity<?> streamMp4Video(@PathVariable("id") Integer videoId) {
        log.info("🎬 Received request to stream mp4  video with id={}", videoId);

        return videoStreamService.generatemp4PresignedUrl(videoId)
                .<ResponseEntity<?>>map(url -> {
                    log.info("📡 Returning presigned URL for videoId={}", videoId);
                    return ResponseEntity.ok().body(url);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Failed to find or generate URL for videoId={}", videoId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Video not found or presigned URL generation failed");
                });
    }
}

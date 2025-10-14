package com.jp.streamspace.vidoestream.dto;


import java.util.UUID;

public class PresignResponse {
    private int videoId;
    private String presignedUrl;
    private String s3Key;

    public PresignResponse() {}

    public PresignResponse(int videoId, String presignedUrl, String s3Key) {
        this.videoId = videoId;
        this.presignedUrl = presignedUrl;
        this.s3Key = s3Key;
    }

    public int getVideoId() { return videoId; }
    public void setVideoId(int videoId) { this.videoId = videoId; }
    public String getPresignedUrl() { return presignedUrl; }
    public void setPresignedUrl(String presignedUrl) { this.presignedUrl = presignedUrl; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
}

package com.jp.streamspace.vidoestream.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NotifyUploadRequest {
    private int videoId;
    private String s3Key; // path used to upload (so backend can verify HEAD)

    public int getVideoId() { return videoId; }
    public void setVideoId(int videoId) { this.videoId = videoId; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
}

package com.jp.streamspace.vidoestream.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VideoStreamResponse {
    private Integer id;
    private String title;
    private String description;
    private String presignedUrl;
}

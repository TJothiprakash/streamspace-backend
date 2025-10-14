package com.jp.streamspace.vidoestream.modal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;

@Alias("Video")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video implements Serializable {
    private static final long serialVersionUID = 1L;

    // Auto-increment primary key
    private Integer id;

    private Integer uploadedBy;
    private Integer durationSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPrivate;
    private String title;
    private String description;
    private String status;
    private String masterKey;
    private String thumbnailUrl;
}

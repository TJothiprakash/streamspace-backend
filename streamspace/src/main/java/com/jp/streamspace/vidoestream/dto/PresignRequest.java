package com.jp.streamspace.vidoestream.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignRequest {
    private String filename;   // original filename from client
    private Integer uploadedBy;
    private Boolean isPrivate;
    private String title;
    private String description;

    // getters & setters
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public Integer getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Integer uploadedBy) { this.uploadedBy = uploadedBy; }
    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

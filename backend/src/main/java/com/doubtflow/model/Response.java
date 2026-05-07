package com.doubtflow.model;

import java.time.LocalDateTime;

public class Response {

    private Long id;
    private Long doubtId;
    private Long mentorId;
    private String responseText;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoubtId() {
        return doubtId;
    }

    public void setDoubtId(Long doubtId) {
        this.doubtId = doubtId;
    }

    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

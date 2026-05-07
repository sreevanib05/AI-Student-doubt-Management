package com.doubtflow.model;

import com.doubtflow.interfaces.Assignable;

import java.time.LocalDateTime;

public class Doubt implements Assignable {

    protected Long id;
    protected String title;
    protected String description;
    protected DoubtCategory category;
    protected DoubtStatus status;
    protected Long studentId;
    protected Long mentorId;
    protected Mentor mentor;
    protected String studentName;
    protected String mentorName;
    protected String latestResponse;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected LocalDateTime resolvedAt;

    @Override
    public void assignMentor(Mentor mentor) {
        this.mentor = mentor;
        this.mentorId = mentor.getId();
    }

    public void updateStatus(DoubtStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DoubtCategory getCategory() {
        return category;
    }

    public void setCategory(DoubtCategory category) {
        this.category = category;
    }

    public DoubtStatus getStatus() {
        return status;
    }

    public void setStatus(DoubtStatus status) {
        this.status = status;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public Mentor getMentor() {
        return mentor;
    }

    public void setMentor(Mentor mentor) {
        this.mentor = mentor;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public String getLatestResponse() {
        return latestResponse;
    }

    public void setLatestResponse(String latestResponse) {
        this.latestResponse = latestResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}

package com.doubtflow.model;

public enum DoubtStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED;

    public static DoubtStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status is required.");
        }

        try {
            return DoubtStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Status must be OPEN, ASSIGNED, IN_PROGRESS, or RESOLVED.");
        }
    }
}

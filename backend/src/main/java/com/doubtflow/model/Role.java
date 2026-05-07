package com.doubtflow.model;

public enum Role {
    STUDENT,
    MENTOR,
    ADMIN;

    public static Role from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }

        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Role must be STUDENT, MENTOR, or ADMIN.");
        }
    }
}

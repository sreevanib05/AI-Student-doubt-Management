package com.doubtflow.dto;

public record AuthResponse(String token, String role, Long id, String name, String email) {
}

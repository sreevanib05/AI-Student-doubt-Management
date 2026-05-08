package com.doubtflow.dto;

public record DoubtAttachment(String fileName, String contentType, byte[] data) {
}

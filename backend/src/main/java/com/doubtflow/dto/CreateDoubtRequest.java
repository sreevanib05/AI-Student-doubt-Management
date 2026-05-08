package com.doubtflow.dto;

public record CreateDoubtRequest(
        String title,
        String description,
        String category,
        String subject,
        String contextNotes,
        String promptTemplate,
        String pdfFileName,
        String pdfContentType,
        String pdfData
) {
}

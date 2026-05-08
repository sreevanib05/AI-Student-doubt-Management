package com.doubtflow.dto;

import java.util.List;

public record AnalyticsResponse(
        long totalDoubts,
        long openDoubts,
        long assignedDoubts,
        long inProgressDoubts,
        long resolvedDoubts,
        long totalStudents,
        long totalMentors,
        long pdfAttachmentCount,
        long contextRichDoubts,
        double averageResolutionHours,
        List<CategoryStat> categoryStats,
        List<SubjectStat> subjectStats,
        List<MentorWorkload> mentorWorkloads
) {
}

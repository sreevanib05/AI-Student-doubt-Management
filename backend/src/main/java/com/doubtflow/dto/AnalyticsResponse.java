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
        List<CategoryStat> categoryStats,
        List<MentorWorkload> mentorWorkloads
) {
}

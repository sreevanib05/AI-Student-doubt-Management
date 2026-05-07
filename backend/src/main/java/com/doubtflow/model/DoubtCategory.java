package com.doubtflow.model;

import com.doubtflow.exception.InvalidCategoryException;

public enum DoubtCategory {
    CONCEPTUAL,
    CODING,
    DEBUGGING;

    public static DoubtCategory from(String value) throws InvalidCategoryException {
        if (value == null || value.isBlank()) {
            throw new InvalidCategoryException("Doubt category is required.");
        }

        try {
            return DoubtCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidCategoryException("Category must be CONCEPTUAL, CODING, or DEBUGGING.");
        }
    }
}

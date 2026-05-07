package com.doubtflow.model;

import com.doubtflow.exception.InvalidCategoryException;

public final class DoubtFactory {

    private DoubtFactory() {
    }

    public static Doubt createDoubt(String categoryValue) throws InvalidCategoryException {
        DoubtCategory category = DoubtCategory.from(categoryValue);

        return switch (category) {
            case CONCEPTUAL -> new ConceptualDoubt();
            case CODING -> new CodingDoubt();
            case DEBUGGING -> new DebuggingDoubt();
        };
    }
}

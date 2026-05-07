package com.doubtflow.model;

public class CodingDoubt extends Doubt {

    private String programmingLanguage;

    public CodingDoubt() {
        this.category = DoubtCategory.CODING;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }
}

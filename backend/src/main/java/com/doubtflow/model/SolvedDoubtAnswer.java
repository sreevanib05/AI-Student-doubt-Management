package com.doubtflow.model;

public class SolvedDoubtAnswer {

    private Long doubtId;
    private String title;
    private String description;
    private String category;
    private String responseText;

    public SolvedDoubtAnswer(Long doubtId, String title, String description, String category, String responseText) {
        this.doubtId = doubtId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.responseText = responseText;
    }

    public Long getDoubtId() {
        return doubtId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getResponseText() {
        return responseText;
    }
}

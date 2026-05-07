package com.doubtflow.model;

public class ConceptualDoubt extends Doubt {

    private String topic;

    public ConceptualDoubt() {
        this.category = DoubtCategory.CONCEPTUAL;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}

package com.doubtflow.model;

public class DebuggingDoubt extends Doubt {

    private String errorMessage;

    public DebuggingDoubt() {
        this.category = DoubtCategory.DEBUGGING;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

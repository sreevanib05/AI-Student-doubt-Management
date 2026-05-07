package com.doubtflow.thread;

import com.doubtflow.model.Mentor;

import java.util.concurrent.Callable;

public class MentorResolutionWorker implements Callable<String> {

    private final Mentor mentor;

    public MentorResolutionWorker(Mentor mentor) {
        this.mentor = mentor;
    }

    @Override
    public String call() throws Exception {
        Thread.sleep(700);
        return mentor.getName() + " checked assigned doubts on thread " + Thread.currentThread().getName();
    }
}

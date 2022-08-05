package com.ulearning.video.ffmpeg.task;

import java.util.concurrent.Callable;

public abstract class TaskWithResult implements Callable<Integer> {
    private int id;

    public TaskWithResult(int id) {
        this.id = id;
    }

    @Override
    public abstract Integer call() throws Exception;

    public int getId() {
        return id;
    }
}
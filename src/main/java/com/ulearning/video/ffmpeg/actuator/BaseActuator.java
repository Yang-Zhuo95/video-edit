package com.ulearning.video.ffmpeg.actuator;

/**
 * @author yangzhuo
 * @description 基础执行器
 * @date 2022-08-02 14:04
 */
public abstract class BaseActuator implements Actuator {
    @Override
    public Integer execute() {
        throw new IllegalArgumentException("该任务不支持直接执行");
    }
}

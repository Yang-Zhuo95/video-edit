package com.ulearning.video.ffmpeg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description ffmPeg 合并请求配置
 * @date 2022-08-08 16:10
 */
@Configuration
@ConfigurationProperties(prefix = "video.edit.merge")
public class FfmPegMergeConfig {
    /**
     * 合并请求工作线程池核心线程数
     */
    private Integer workCorePoolSize;

    /**
     * 合并请求工作线程池阻塞队列长度
     */
    private Integer workQueueCapacity;


    /**
     * 合并请求一轮可执行最大任务数
     */
    private Integer maxTaskSize;

    /**
     * 执行合并请求的间隔时间
     */
    private Long period;

    /**
     * 合并请求工作线程池核心线程数
     */
    public static Integer WORK_CORE_POOL_SIZE;

    /**
     * 合并请求工作线程池阻塞队列长度
     */
    public static Integer WORK_QUEUE_CAPACITY;


    /**
     * 合并请求一轮可执行最大任务数
     */
    public static Integer MAX_TASK_SIZE;

    /**
     * 执行合并请求的间隔时间
     */
    public static Long PERIOD;

    @PostConstruct
    private void ffmPegConfigValues() throws UnknownHostException {
        WORK_CORE_POOL_SIZE = Objects.nonNull(this.workCorePoolSize) ? this.workCorePoolSize : 20;
        WORK_QUEUE_CAPACITY = Objects.nonNull(this.workQueueCapacity) ? this.workQueueCapacity : 2000;
        MAX_TASK_SIZE = Objects.nonNull(this.maxTaskSize) ? this.maxTaskSize : 200;
        PERIOD = Objects.nonNull(this.period) ? this.period : 200;
    }

    public Integer getWorkCorePoolSize() {
        return workCorePoolSize;
    }

    public void setWorkCorePoolSize(Integer workCorePoolSize) {
        this.workCorePoolSize = workCorePoolSize;
    }

    public Integer getWorkQueueCapacity() {
        return workQueueCapacity;
    }

    public void setWorkQueueCapacity(Integer workQueueCapacity) {
        this.workQueueCapacity = workQueueCapacity;
    }

    public Integer getMaxTaskSize() {
        return maxTaskSize;
    }

    public void setMaxTaskSize(Integer maxTaskSize) {
        this.maxTaskSize = maxTaskSize;
    }

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }
}

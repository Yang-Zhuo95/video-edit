package com.ulearning.video.subtitle.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangzhuo
 * @description 字幕任务配置类
 * @date 2022-09-22 9:32
 */
@Configuration
@ConditionalOnClass(SubtitleTask.class)
public class SubtitleTaskConfig {

    /**
     * 异步转换任务线程池 -核心线程数
     */
    @Value("${subtitle.thread-pool.corePoolSize}")
    public Integer corePoolSize;

    /**
     * 异步转换任务线程池 -最大线程数
     */
    @Value("${subtitle.thread-pool.maximumPoolSize}")
    public Integer maximumPoolSize;

    /**
     * 异步转换任务线程池 -等待队列大小
     */
    @Value("${subtitle.thread-pool.queueCapacity}")
    public Integer queueCapacity;

    /**
     * 异步转换任务线程池 -轮询任务执行间隔
     */
    @Value("${subtitle.task.period}")
    public Long period;

    /**
     * 异步转换任务线程池 -每次轮询执行的任务数量
     */
    @Value("${subtitle.task.size}")
    public Integer size;
}

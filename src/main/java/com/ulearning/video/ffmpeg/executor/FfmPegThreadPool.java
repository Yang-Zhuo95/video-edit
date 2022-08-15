package com.ulearning.video.ffmpeg.executor;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.ulearning.video.ffmpeg.config.FfmPegConfig;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yangzhuo
 * @description ffmpeg线程池
 * @date 2022-08-01 14:47
 */
public class FfmPegThreadPool {

    private FfmPegThreadPool() {
        throw new IllegalStateException("Utility class");
    }

    private static class HolderClass{
        /**
         * ffmPeg线程池
         */
        private static final ThreadPoolExecutor EXECUTOR;

        /**
         * 包装成ttl线程池
         */
        private static final ExecutorService TTL_EXECUTOR;

        /**
         * 合并请求 Boss 线程池, 延迟安排任务
         */
        private static final ScheduledExecutorService MERGE_BOSS_EXECUTOR;

        /**
         * 合并请求 Work 线程池, 负责执行任务
         */
        private static final ExecutorService MERGE_WORK_EXECUTOR;

        static {
            CustomizableThreadFactory ffmPegAsync = new CustomizableThreadFactory("ffmPeg-asyncThreadPool-");
            ffmPegAsync.setDaemon(true);
            EXECUTOR = new ThreadPoolExecutor(FfmPegConfig.CORE_POOL_SIZE, FfmPegConfig.CORE_POOL_SIZE,
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(FfmPegConfig.QUEUE_CAPACITY),
                    ffmPegAsync, getRejectedExecution(FfmPegConfig.REJECT_POLICY));
            TTL_EXECUTOR = TtlExecutors.getTtlExecutorService(EXECUTOR);

            // 请求合并boss线程池
            CustomizableThreadFactory ffmPegMergeBoss = new CustomizableThreadFactory("ffmPeg-merge-bossThreadPool-");
            ffmPegMergeBoss.setDaemon(true);
            MERGE_BOSS_EXECUTOR = TtlExecutors.getTtlScheduledExecutorService(new ScheduledThreadPoolExecutor(1, ffmPegMergeBoss));

            // 请求合并work线程池
            CustomizableThreadFactory ffmPegMergeWork = new CustomizableThreadFactory("ffmPeg-merge-workThreadPool-");
            ffmPegMergeWork.setDaemon(true);
            MERGE_WORK_EXECUTOR = TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(FfmPegConfig.CORE_POOL_SIZE, FfmPegConfig.CORE_POOL_SIZE,
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(FfmPegConfig.QUEUE_CAPACITY),
                    ffmPegMergeWork, getRejectedExecution(FfmPegConfig.REJECT_POLICY)));

        }

        private static RejectedExecutionHandler getRejectedExecution(String string) {
            switch (string) {
                case "CallerRun":
                    return new ThreadPoolExecutor.CallerRunsPolicy();
                default:
                    return new ThreadPoolExecutor.DiscardPolicy();
            }
        }
    }

    public static ThreadPoolExecutor getThreadPool() {
        return HolderClass.EXECUTOR;
    }

    public static ExecutorService getExecutor() {
        return HolderClass.TTL_EXECUTOR;
    }

    public static ScheduledExecutorService getMergeBossExecutor() {
        return HolderClass.MERGE_BOSS_EXECUTOR;
    }

    public static ExecutorService getMergeWorkExecutor() {
        return HolderClass.MERGE_WORK_EXECUTOR;
    }
}

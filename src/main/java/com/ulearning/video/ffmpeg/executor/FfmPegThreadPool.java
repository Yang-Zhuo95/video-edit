package com.ulearning.video.ffmpeg.executor;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.ulearning.video.ffmpeg.config.FfmPegConfig;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yangzhuo
 * @description ffmpeg线程池
 * @date 2022-08-01 14:47
 */
public class FfmPegThreadPool {

    private static class HolderClass{
        /**
         * ffmPeg线程池
         */
        private static final ThreadPoolExecutor EXECUTOR;

        /**
         * 包装成ttl线程池
         */
        private static final ExecutorService TTL_EXECUTOR;

        static {
            EXECUTOR = new ThreadPoolExecutor(FfmPegConfig.CORE_POOL_SIZE, FfmPegConfig.CORE_POOL_SIZE,
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(FfmPegConfig.QUEUE_CAPACITY),
                    new CustomizableThreadFactory("ffmPeg-asyncThreadPool-"),
                    getRejectedExecution(FfmPegConfig.REJECT_POLICY));
            TTL_EXECUTOR = TtlExecutors.getTtlExecutorService(EXECUTOR);
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

    public static ExecutorService getExecutor() {
        return HolderClass.TTL_EXECUTOR;
    }

    public static ThreadPoolExecutor getThreadPool() {
        return HolderClass.EXECUTOR;
    }
}

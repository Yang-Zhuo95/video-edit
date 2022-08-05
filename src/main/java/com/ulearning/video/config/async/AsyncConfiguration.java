package com.ulearning.video.config.async;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yangzhuo
 * @description 异步线程池配置类
 * @date 2022-07-27 14:01
 */
@Component
public class AsyncConfiguration {
    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        // 自定义线程池
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程数
        taskExecutor.setCorePoolSize(10);
        // 最大线程数
        taskExecutor.setMaxPoolSize(40);
        // 线程队列大小
        taskExecutor.setQueueCapacity(200);
        // 线程池中的线程的名称前缀
        taskExecutor.setThreadNamePrefix("exam-asyncThreadPool-");
        // 设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
        taskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        // 非核心线程的存活时间
        taskExecutor.setKeepAliveSeconds(60);
        /*
            拒绝处理策略
            CallerRunsPolicy()：交由调用方线程运行，比如 main 线程。
            AbortPolicy()：直接抛出异常。
            DiscardPolicy()：直接丢弃。
            DiscardOldestPolicy()：丢弃队列中最老的任务。
        */
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        taskExecutor.initialize();
        return TtlExecutors.getTtlExecutorService(taskExecutor.getThreadPoolExecutor());
    }
}

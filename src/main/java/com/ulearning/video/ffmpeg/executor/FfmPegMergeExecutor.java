package com.ulearning.video.ffmpeg.executor;

import com.ulearning.video.ffmpeg.actuator.Actuator;
import com.ulearning.video.ffmpeg.task.FutureModel;
import com.ulearning.video.ffmpeg.util.FfmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yangzhuo
 * @description 合并执行ffmPeg命令, 减少相同参数的并发请求
 * @date 2022-08-08 9:39
 */
@Component
@Slf4j
public class FfmPegMergeExecutor {
    /**
     * 任务队列
     */
    private static final ConcurrentLinkedDeque<FutureModel<Actuator, Integer>> TASK_QUEUE = new ConcurrentLinkedDeque<>();

    /**
     * Boss 线程池, 延迟安排任务
     */
    private static final ScheduledExecutorService BOSS_EXECUTOR = FfmPegThreadPool.getMergeBossExecutor();

    /**
     * Work 线程池, 负责执行任务
     */
    private static final ExecutorService WORK_EXECUTOR = FfmPegThreadPool.getMergeWorkExecutor();


    @PostConstruct
    public void init() {
        BOSS_EXECUTOR.scheduleAtFixedRate(getRunnable(100), 0L, 200, TimeUnit.MILLISECONDS);
    }

    private Runnable getRunnable(int maxRequestSize) {
        return () -> {
            int queueSize = TASK_QUEUE.size();
            int size = Math.min(queueSize, maxRequestSize);
            // 合并队列无任务, 直接返回
            if (size == 0) {
                return;
            }
            // 获取指定任务数
            List<FutureModel<Actuator, Integer>> futureModels = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                futureModels.add(TASK_QUEUE.poll());
            }
            // 合并任务列表
            Map<Actuator, List<FutureModel<Actuator, Integer>>> actuatorMap = futureModels.stream()
                    .collect(Collectors.groupingBy(FutureModel::getActuator));
            // 遍历执行
            for (Actuator actuator : actuatorMap.keySet()) {
                // 对于参数重复的任务, 只执行一次
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(actuator::execute, WORK_EXECUTOR);
                // 执行完成后回调
                future.whenComplete((r, t) -> {
                    File outputFile = actuator.getOutputFile();
                    // 获取参数相同的所有任务
                    List<FutureModel<Actuator, Integer>> futureModelList = actuatorMap.get(actuator);
                    if (Objects.nonNull(t)) {
                        log.error("执行合并请求异常|{} |{}", actuator, t.getMessage());
                        r = FfmpegUtil.CODE_FAIL;
                    }
                    final Integer result = r;
                    // 遍历参数相同的所有任务, 返回响应
                    futureModelList.forEach(f -> {
                        f.getActuator().setOutputFile(outputFile);
                        f.getFuture().complete(result);
                    });
                });
            }
        };
    }

    public static Integer execute(Actuator actuator) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        TASK_QUEUE.add(new FutureModel<>(actuator, future));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException var4) {
            var4.printStackTrace();
            return null;
        }
    }

}

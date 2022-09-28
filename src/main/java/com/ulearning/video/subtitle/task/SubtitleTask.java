package com.ulearning.video.subtitle.task;

import cn.hutool.core.collection.CollUtil;
import com.ulearning.video.common.distributedLock.DistributedLockAnnotation;
import com.ulearning.video.common.utils.subtitle.AudioSubtitleFactory;
import com.ulearning.video.common.utils.subtitle.eneity.Result;
import com.ulearning.video.common.utils.subtitle.eneity.SubtitleObject;
import com.ulearning.video.subtitle.dao.AudioSubtitleDao;
import com.ulearning.video.subtitle.model.AudioSubtitleModel;
import com.ulearning.video.subtitle.service.AudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yangzhuo
 * @description 字幕任务类
 * @date 2022-09-22 9:30
 */
@Slf4j
@Component
public class SubtitleTask {

    @Autowired
    private SubtitleTaskConfig subtitleTaskConfig;

    @Autowired
    private AudioSubtitleDao audioSubtitleDao;

    @Autowired
    private AudioService audioService;

    /**
     * 异步转换任务线程池 (提供给该业务调用者使用)
     */
    public static ThreadPoolExecutor executor;

    /**
     * 轮询线程池 定时查询未完成的转换任务
     */
    private static ScheduledThreadPoolExecutor boss;

    /**
     * 查询任务信息线程池,实现查询和信息落库 IO密集型
     */
    private static ThreadPoolExecutor work;

    @PostConstruct
    private void init() {
        CustomizableThreadFactory subtitleThreadFactory = new CustomizableThreadFactory("subtitle-asyncThreadPool-");
        subtitleThreadFactory.setDaemon(true);
        // 异步任务线程池 (提供给该业务调用者使用)
        executor = new ThreadPoolExecutor(subtitleTaskConfig.corePoolSize, subtitleTaskConfig.maximumPoolSize,
                0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(subtitleTaskConfig.queueCapacity),
                subtitleThreadFactory);

        // 轮询任务定时器线程池 (定时执行轮询任务)
        CustomizableThreadFactory subtitleBoss = new CustomizableThreadFactory("subtitle-bossThreadPool-");
        subtitleBoss.setDaemon(true);
        boss = new ScheduledThreadPoolExecutor(1, subtitleBoss);

        // 查询任务信息线程池,实现查询和信息落库 IO密集型
        CustomizableThreadFactory subtitleWork = new CustomizableThreadFactory("subtitle-asyncThreadPool-");
        subtitleWork.setDaemon(true);
        int processors = Runtime.getRuntime().availableProcessors();
        work = new ThreadPoolExecutor(processors, processors * 2, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(subtitleTaskConfig.queueCapacity), subtitleWork);

        // 将轮询任务添加到boss线程池中
        boss.scheduleAtFixedRate(pollingTask(subtitleTaskConfig.size), 0L, subtitleTaskConfig.period, TimeUnit.MILLISECONDS);
    }

    /**
     * 轮询未完成的任务列表,并执行查询任务
     * @param maxSize 每次轮询最大查询数
     */
    private Runnable pollingTask(Integer maxSize) {
        return () -> {
            polling(maxSize);
        };
    }

    @DistributedLockAnnotation
    public void polling(Integer maxSize) {
        // 查询数据库中未完成的任务列表
        List<AudioSubtitleModel> tasks = audioSubtitleDao.findNeedPollingList(maxSize);
        if (CollUtil.isEmpty(tasks)) {
            log.debug("无需执行, 轮询任务列表为空");
            return;
        }
        // 将任务丢到work线程池中,查询落库
        for (AudioSubtitleModel task : tasks) {
            CompletableFuture.runAsync(() -> {
                audioService.saveSubtitle(task);
            }, work);
        }
    }

}

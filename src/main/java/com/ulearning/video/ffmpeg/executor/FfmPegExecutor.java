package com.ulearning.video.ffmpeg.executor;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.ulearning.video.ffmpeg.actuator.Actuator;
import com.ulearning.video.ffmpeg.cache.FfmPegCache;
import com.ulearning.video.ffmpeg.config.FfmPegConfig;
import com.ulearning.video.ffmpeg.dao.VideoEditRecordDao;
import com.ulearning.video.ffmpeg.entity.TaskInfo;
import com.ulearning.video.ffmpeg.model.VideoEditRecordModel;
import com.ulearning.video.utils.ApplicationUtils;
import com.ulearning.video.ffmpeg.util.FfmpegUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @author yangzhuo
 * @description ffmPeg执行器
 * @date 2022-07-28 9:01
 */
@Slf4j
public class FfmPegExecutor {

    private static ThreadPoolExecutor threadPool = FfmPegThreadPool.getThreadPool();

    private static ExecutorService ttlExecutor = FfmPegThreadPool.getExecutor();

    /**
     * 环境变量数据
     */
    public static final TransmittableThreadLocal<Map<String, Object>> VALUE = new TransmittableThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new ConcurrentHashMap<>();
        }

        //解决普通线程池中使用TTL，造成数据污染的问题
        @Override
        protected Map<String, Object> childValue(Map<String, Object> parentValue) {
            return initialValue();
        }

        //父子线程使用的是拷贝对象。而非简单对象的引用。
        @Override
        public Map<String, Object> copy(Map<String, Object> parentValue) {
            return new ConcurrentHashMap<>(parentValue);
        }
    };

    /**
     * 任务id,不接入数据库的情况下会用到
     */
    private static AtomicInteger localTaskId = new AtomicInteger();

    public static final Integer SUCCESS = 0;
    public static final Integer ERROR = 1;

    /**
     * 一些默认值, 执行前可以手动设置
     */
    private static void defaultValues() {
        // 设置默认字体
        if (StrUtil.isBlank(getFont())) {
            setFont(FfmPegConfig.FONT);
        }
        // 无信号源默认文本
        if (StrUtil.isBlank(getNoSourceInfo())) {
            setNoSourceInfo(FfmPegConfig.NO_SOURCE_INFO);
        }
        // 默认线程数
        if (Objects.isNull(getThreads())) {
            setThreads(FfmPegConfig.WORK_THREADS);
        }
    }

    /**
     * 设置任务id
     */
    public static void setTaskId(int taskId) {
        VALUE.get().put("taskId", taskId);
    }

    /**
     * 获取任务id
     */
    public static Integer getTaskId() {
        return (Integer) VALUE.get().get("taskId");
    }


    /**
     * 获取正在运行的线程数
     */
    public static int getActiveCount() {
        return threadPool.getActiveCount();
    }

    /**
     * 获取已执行任务总数
     */
    public static long getCompletedTaskCount() {
        return threadPool.getCompletedTaskCount();
    }

    /**
     * 获取等待队列长度
     */
    public static long getWaitQueueSize() {
        return threadPool.getQueue().size();
    }

    /**
     * @description 设置string类型数据
     * @date 2022/7/29 17:43
     * @author yangzhuo
     */
    public static void setString(String key, String value) {
        VALUE.get().put(key, value);
    }

    /**
     * @description 获取string类型数据
     * @date 2022/7/29 17:43
     * @author yangzhuo
     */
    public static String getString(String key) {
        return (String) VALUE.get().get(key);
    }

    /**
     * @description 设置Integer类型数据
     * @date 2022/7/29 17:43
     * @author yangzhuo
     */
    public static void setInteger(String key, Integer value) {
        VALUE.get().put(key, value);
    }

    /**
     * @description 获取Integer类型数据
     * @date 2022/7/29 17:43
     * @author yangzhuo
     */
    public static Integer getInteger(String key) {
        return (Integer) VALUE.get().get(key);
    }

    /**
     * 设置执行任务的线程数
     */
    public static void setThreads(Integer threads) {
        setInteger("threads", threads);
    }

    /**
     * 获取执行任务的线程数
     */
    public static Integer getThreads() {
        return getInteger("threads");
    }

    /**
     * 设置信息
     */
    public static void setMsg(String msg) {
        setString("msg", msg);
    }

    /**
     * 获取信息
     */
    public static String getMsg() {
        return getString("msg");
    }

    /**
     * 设置输出路径
     */
    public static void setPath(String path) {
        setString("path", path);
    }

    /**
     * 获取输出路径
     */
    public static String getPath() {
        return getString("path");
    }

    /**
     * 设置正在执行的进度
     */
    public static void setDuration(String duration) {
        setString("duration", duration);
    }

    /**
     * 获取正在进行的进度
     */
    public static String getDuration() {
        return getString("duration");
    }

    /**
     * 设置总时长进度
     */
    public static void setAllDuration(String allDuration) {
        setString("allDuration", allDuration);
    }

    /**
     * 获取总时长进度
     */
    public static String getAllDuration() {
        return getString("allDuration");
    }

    /**
     * 设置总时长进度
     */
    public static void setFont(String font) {
        setString("font", font);
    }

    /**
     * 获取总时长进度
     */
    public static String getFont() {
        return getString("font");
    }

    /**
     * @description 无信号源提示文本
     * @date 2022/7/29 17:43
     * @author yangzhuo
     */
    public static void setNoSourceInfo(String noSourceInfo) {
        setString("noSourceInfo", noSourceInfo);
    }

    /**
     * @description 无信号源提示文本
     * @date 2022/7/29 17:43
     * @author yangzhuo
     */
    public static String getNoSourceInfo() {
        return getString("noSourceInfo");
    }

    /**
     * 清理threadLocal中的值, 防止线程池复用时数据污染
     * @date 2022/7/28 9:06
     * @author yangzhuo
     */
    public static void clear() {
        VALUE.remove();
    }

    /**
     * 无返回值执行(默认回调)
     * @param cmd 需要执行的cmd
     * @date 2022/7/28 9:06
     * @author yangzhuo
     */
    public static TaskInfo execute(String cmd) {
        return execute(cmd, null);
    }

    /**
     * 无返回值执行(自定义回调)
     * @param cmd        需要执行的cmd
     * @param biConsumer 执行结束的回调处理
     * @date 2022/7/28 9:06
     * @author yangzhuo
     */
    public static TaskInfo execute(String cmd, BiConsumer<Integer, Throwable> biConsumer) {
        Objects.requireNonNull(cmd, "不能执行空命令");
        TaskInfo taskInfo = createTaskInfo();
        if (taskInfo.getThreadPoolFull()) {
            return taskInfo;
        }
        Integer taskId = recordCmd(cmd);
        // 执行ffmpeg命令
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            if (!updateStatus(taskId, VideoEditRecordModel.NOT_STARTED, VideoEditRecordModel.STARTED)) {
                return VideoEditRecordModel.STOPPED;
            }
            return FfmpegUtil.cmdExecute(cmd);
        }, ttlExecutor);
        // 注册执行回调事件
        future.handle(FfmPegExecutor::callback);
        // 注册额外的回调事件
        if (Objects.nonNull(biConsumer)) {
            future.whenComplete(biConsumer);
        }
        taskInfo.setTaskId(getTaskId());
        FfmPegCache.putTaskInfo(taskInfo);
        return taskInfo;
    }

    public static TaskInfo createCmdAndExecute(Actuator actuator) {
        return createCmdAndExecute(actuator, null);
    }

    /**
     * 异步生成cmd命令并执行
     * @date 2022/7/28 9:06
     * @author yangzhuo
     */
    public static TaskInfo createCmdAndExecute(Actuator actuator, BiConsumer<Integer, Throwable> biConsumer) {
        TaskInfo taskInfo = createTaskInfo();
        // 没有指定输出路径的情况, 使用默认输出路径
        if (StrUtil.isNotBlank(actuator.getOutputPath())) {
            setPath(actuator.getOutputPath());
        } else {
            setPath(FfmPegConfig.OUTPUT_SPACE + IdUtil.getSnowflakeNextId() + ".mp4");
        }
        if (taskInfo.getThreadPoolFull()) {
            return taskInfo;
        }
        // 创建任务记录
        int taskId = recordCmd(actuator.toString());
        setTaskId(taskId);
        // 创建ffmpeg命令并执行
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(
                () -> {
                    if (!updateStatus(taskId, VideoEditRecordModel.NOT_STARTED, VideoEditRecordModel.STARTED)) {
                        return VideoEditRecordModel.STOPPED;
                    }
                    String cmd = actuator.createCmd();
                    Objects.requireNonNull(cmd, "不能执行空命令");
                    // 更新记录信息
                    updateRecord(VideoEditRecordModel.builder()
                            .id(getTaskId()).cmd(cmd).status(VideoEditRecordModel.STARTED)
                            .build());
                    return FfmpegUtil.cmdExecute(cmd);
                }, ttlExecutor
        );
        // 注册执行回调事件
        future.handle(FfmPegExecutor::callback);
        // 注册额外的回调事件
        if (Objects.nonNull(biConsumer)) {
            future.whenComplete(biConsumer);
        }
        taskInfo.setTaskId(getTaskId());
        FfmPegCache.putTaskInfo(taskInfo);
        return taskInfo;
    }

    /**
     * 默认的执行回调
     * @param result    执行结果
     * @param throwable 异常信息
     * @date 2022/7/29 17:23
     * @author yangzhuo
     */
    private static Integer callback(Integer result, Throwable throwable) {
        Integer taskId = getTaskId();
        String msg = getMsg();
        // 删除缓存中记录的任务信息
        removeCache(taskId);
        // 异常处理
        if (Objects.nonNull(throwable)) {
            String message = throwable.getMessage();
            String showMsg = Objects.nonNull(msg) ? msg : message;
            updateRecord(VideoEditRecordModel.error(taskId, showMsg));
            new File(getPath()).delete();
            log.error("ffmPeg执行异常| taskId {}| msg {}", taskId, showMsg);
            return VideoEditRecordModel.ERROR;
        }

        if (FfmPegExecutor.SUCCESS.equals(result)) {
            // 成功处理
            log.info("ffmPeg执行成功| taskId {}", taskId);
            updateRecord(VideoEditRecordModel.success(taskId));
        } else if (VideoEditRecordModel.STOPPED.equals(result)) {
            // 手动停止
            log.info("ffmPeg手动停止| taskId {}", taskId);
            updateRecord(VideoEditRecordModel.stopped(taskId, "手动停止"));
            new File(getPath()).delete();
        } else {
            // 失败处理
            log.error("ffmPeg执行失败| taskId {}| msg {}", taskId, msg);
            updateRecord(VideoEditRecordModel.fail(taskId, msg));
            new File(getPath()).delete();
        }
        return result;
    }

    /**
     * 创建一条空命令的记录
     * @date 2022/7/29 16:34
     * @author yangzhuo
     */
    private static int insertRecord() {
        recordCmd("");
        return getTaskId();
    }

    /**
     * 创建记录
     * @param cmd cmd命令
     * @date 2022/7/29 16:34
     * @author yangzhuo
     */
    private static Integer recordCmd(String cmd) {
        VideoEditRecordDao videoEditRecordDao = ApplicationUtils.getBean(VideoEditRecordDao.class);
        int taskId;
        if (Objects.nonNull(videoEditRecordDao)) {
            VideoEditRecordModel creat = VideoEditRecordModel.creat(cmd, getPath());
            videoEditRecordDao.insert(creat);
            taskId = creat.getId();
        } else {
            taskId = localTaskId.incrementAndGet();
        }
        setTaskId(taskId);
        log.info("FfmPeg执行任务已创建,taskId: {}", taskId);
        return taskId;
    }

    /**
     * 更新记录
     * @param videoEditRecordModel 视频剪辑记录
     * @date 2022/7/29 16:38
     * @author yangzhuo
     */
    private static void updateRecord(VideoEditRecordModel videoEditRecordModel) {
        VideoEditRecordDao videoEditRecordDao = ApplicationUtils.getBean(VideoEditRecordDao.class);
        if (Objects.nonNull(videoEditRecordDao)) {
            videoEditRecordDao.update(videoEditRecordModel);
        }
    }

    private static boolean updateStatus(int taskId, int oldStatus, int newStatus) {
        VideoEditRecordDao videoEditRecordDao = ApplicationUtils.getBean(VideoEditRecordDao.class);
        if (Objects.nonNull(videoEditRecordDao)) {
            return videoEditRecordDao.updateStatus(taskId, oldStatus, newStatus) == 1;
        }
        return true;
    }

    private static void removeCache(Integer taskId) {
        FfmPegCache.removeTaskInfo(taskId);
    }

    private static TaskInfo createTaskInfo() {
        // 获取当前线程池信息
        // 正在运行的线程数
        int activeCount = getActiveCount();
        // 等待中的线程数
        long waitCount = getWaitQueueSize();
        long completedTaskCount = getCompletedTaskCount();
        TaskInfo taskInfo = TaskInfo.builder()
                .corePoolSize(FfmPegConfig.CORE_POOL_SIZE)
                .threadPoolFull(waitCount >= FfmPegConfig.QUEUE_CAPACITY)
                .ip(FfmPegConfig.IP).completedTaskCount(completedTaskCount)
                .activeCount(activeCount).waitCount(waitCount).build();
        if (!taskInfo.getThreadPoolFull()) {
            defaultValues();
        }
        return taskInfo;
    }
}

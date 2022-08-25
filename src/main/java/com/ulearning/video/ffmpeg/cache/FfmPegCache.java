package com.ulearning.video.ffmpeg.cache;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheListener;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ulearning.video.ffmpeg.config.FfmPegConfig;
import com.ulearning.video.ffmpeg.entity.TaskInfo;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.File;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description ffmpeg本地缓存信息
 * @date 2022-08-02 11:39
 */
public class FfmPegCache {

    /**
     * 任务信息缓存
     */
    private static final Cache<Integer, TaskInfo> TASK_CACHE = new LRUCache<>(FfmPegConfig.CACHE_CAPACITY);

    /**
     * 任务执行进度缓存
     * MutablePair: 总时长:已完成时长
     */
    private static final Cache<Integer, MutablePair<String, String>> PROGRESS_CACHE = new LRUCache<>(FfmPegConfig.CACHE_CAPACITY);

    /**
     * 文件缓存
     */
    private static final Cache<Object, File> FILE_CACHE;

    private static final Long DEFAULT_TIMEOUT = FfmPegConfig.CACHE_TIMEOUT;

    static {
        FILE_CACHE = new LRUCache<>(FfmPegConfig.CACHE_CAPACITY);
        FILE_CACHE.setListener((key, file) -> {
            FileUtil.del(file);
        });
    }

    public static void putTaskInfo(TaskInfo taskInfo) {
        Integer taskId = taskInfo.getTaskId();
        if (Objects.nonNull(taskId)) {
            TASK_CACHE.put(taskId, taskInfo, DEFAULT_TIMEOUT);
        }
    }

    public static void putTaskInfo(TaskInfo taskInfo, long timeout) {
        Integer taskId = taskInfo.getTaskId();
        if (Objects.nonNull(taskId)) {
            TASK_CACHE.put(taskId, taskInfo, timeout);
        }
    }

    public static TaskInfo getTaskInfo(Integer taskId) {
        return TASK_CACHE.get(taskId);
    }

    public static void removeTaskInfo(Integer taskId) {
        TASK_CACHE.remove(taskId);
    }

    public static void putProgress(int taskId, MutablePair<String, String> progress) {
        if (StrUtil.isBlank(progress.getLeft())) {
            throw new NullPointerException("设置进度必须包含总时长");
        }
        PROGRESS_CACHE.put(taskId, progress, DEFAULT_TIMEOUT);
    }

    public static void putProgress(int taskId, MutablePair<String, String> progress, long timeout) {
        if (StrUtil.isBlank(progress.getLeft())) {
            throw new NullPointerException("设置进度必须包含总时长");
        }
        PROGRESS_CACHE.put(taskId, progress, timeout);
    }

    public static void updateProgress(int taskId, String duration) {
        if (StrUtil.isBlank(duration)) {
            throw new NullPointerException("更新已完成进度不能为空");
        }
        MutablePair<String, String> progress = PROGRESS_CACHE.get(taskId);
        if (Objects.nonNull(progress)) {
            progress.setValue(duration);
        }
    }

    public static MutablePair<String, String> getProgress(int taskId) {
        return PROGRESS_CACHE.get(taskId);
    }

    public static void putFile(Object key, File file) {
        FILE_CACHE.put(key, file, DEFAULT_TIMEOUT);
    }

    public static void putFile(Object key, File file, long timeout) {
        FILE_CACHE.put(key, file, timeout);
    }

    public static File getFile(Object key) {
        return FILE_CACHE.get(key);
    }

    public static boolean containsFileKey(Object key) {
        return FILE_CACHE.containsKey(key);
    }

}

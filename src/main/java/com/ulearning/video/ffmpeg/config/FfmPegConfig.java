package com.ulearning.video.ffmpeg.config;

import cn.hutool.core.io.FileUtil;
import com.ulearning.video.utils.IpUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author yangzhuo
 * @description ffmpeg 配置类
 * @date 2022-08-01 10:34
 */
@Configuration
@ConfigurationProperties(prefix = "video.edit")
public class FfmPegConfig {

    public static Integer WORK_THREADS;
    public static String WORK_SPACE;
    public static String OUTPUT_SPACE;
    public static String FONT;
    public static Integer CORE_POOL_SIZE;
    public static Integer QUEUE_CAPACITY;
    public static String REJECT_POLICY;
    public static String NO_SOURCE_INFO;
    public static String IP;
    public static Integer CACHE_CAPACITY;
    public static Long CACHE_TIMEOUT;
    /**
     * ffmPeg 使用线程数
     */
    private Integer workThreads = 1;
    /**
     * ffmPeg 工作空间
     */
    private String workSpace;
    /**
     * ffmPeg 默认输出空间
     */
    private String outputSpace;
    /**
     * ffmPeg 字体
     */
    private String font;
    /**
     * ffmPeg 线程池核心线程数
     */
    private Integer corePoolSize;
    /**
     * ffmPeg 线程池等待队列长度
     */
    private Integer queueCapacity;
    /**
     * ffmPeg 线程池拒绝策略
     */
    private String rejectPolicy;
    /**
     * ffmPeg 无数据源默认提示信息
     */
    private String noSourceInfo;
    /**
     * ffmPeg 执行信息缓存池大小
     */
    private Integer cacheCapacity;
    /**
     * ffmPeg 缓存过期时间
     */
    private Long cacheTimeout;
    /**
     * 匹配本机ip地址
     */
    private String ipRegex;

    @PostConstruct
    private void ffmPegConfigValues() throws UnknownHostException {
        WORK_THREADS = this.workThreads;
        WORK_SPACE = this.workSpace;
        OUTPUT_SPACE = this.outputSpace;
        FONT = this.font;
        CORE_POOL_SIZE = this.corePoolSize;
        QUEUE_CAPACITY = this.queueCapacity;
        REJECT_POLICY = this.rejectPolicy;
        NO_SOURCE_INFO = this.noSourceInfo;
        IP = IpUtil.matchHostIp(this.ipRegex);
        CACHE_CAPACITY = this.cacheCapacity;
        CACHE_TIMEOUT = this.cacheTimeout;
    }

    public Integer getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(Integer workThreads) {
        this.workThreads = workThreads;
    }

    public String getWorkSpace() {
        return workSpace;
    }

    public void setWorkSpace(String workSpace) {
        FileUtil.mkdir(workSpace);
        this.workSpace = workSpace;
    }

    public String getOutputSpace() {
        return outputSpace;
    }

    public void setOutputSpace(String outputSpace) {
        FileUtil.mkdir(outputSpace);
        this.outputSpace = outputSpace;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public Integer getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(Integer queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getRejectPolicy() {
        return rejectPolicy;
    }

    public void setRejectPolicy(String rejectPolicy) {
        this.rejectPolicy = rejectPolicy;
    }

    public String getNoSourceInfo() {
        return noSourceInfo;
    }

    public void setNoSourceInfo(String noSourceInfo) {
        this.noSourceInfo = noSourceInfo;
    }

    public Integer getCacheCapacity() {
        return cacheCapacity;
    }

    public void setCacheCapacity(Integer cacheCapacity) {
        this.cacheCapacity = cacheCapacity;
    }

    public Long getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(Long cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    public String getIpRegex() {
        return ipRegex;
    }

    public void setIpRegex(String ipRegex) {
        this.ipRegex = ipRegex;
    }
}

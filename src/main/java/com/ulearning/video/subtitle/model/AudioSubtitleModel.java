package com.ulearning.video.subtitle.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description 字幕任务类
 * @date 2022-09-22 10:55
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AudioSubtitleModel {
    /**
     * 执行状态 - 未开始
     */
    public static final Integer NOT_STARTED = 0;

    /**
     * 执行状态 - 格式转换中
     */
    public static final Integer FORMAT_PROGRESS = 1;

    /**
     * 执行状态 - 生成字幕中
     */
    public static final Integer GENERATING_SUBTITLES = 2;

    /**
     * 执行状态 - 生成字幕成功
     */
    public static final Integer SUCCESS = 3;

    /**
     * 执行状态 - 生成字幕失败
     */
    public static final Integer ERROR = 4;

    /**
     * 任务id
     */
    private Integer id;

    /**
     * 执行结束的回调地址
     */
    private String callback;

    /**
     * 源文件地址
     */
    private String sourceUrl;

    /**
     * 音频资源地址
     */
    private String audioUrl;

    /**
     * 使用的三方服务
     */
    private String serviceName;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务id
     */
    private String mongoId;

    /**
     * 执行状态 [0-未开始] , [1-格式转换中], [2-生成字幕中], [3-生成字幕成功], [4--生成字幕失败]
     */
    private Integer status;

    /**
     * 执行信息
     */
    private String msg;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    public static AudioSubtitleModel creat(String sourceUrl, String callback) {
        return AudioSubtitleModel.builder().sourceUrl(sourceUrl).status(NOT_STARTED).callback(callback).build();
    }

    public AudioSubtitleModel error() {
        Objects.requireNonNull(id);
        return AudioSubtitleModel.builder()
                .id(id).callback(callback)
                .msg(msg).status(ERROR).build();
    }

}

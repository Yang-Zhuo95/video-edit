package com.ulearning.video.common.utils.subtitle.eneity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author yangzhuo
 * @description 查询任务结果
 * @date 2022-09-22 16:26
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    /**
     * 转写中
     */
    public static final Integer RUNNING = 0;

    /**
     * 转写成功
     */
    public static final Integer SUCCESS = 1;

    /**
     * 转写失败
     */
    public static final Integer ERROR = 2;

    /**
     * 任务状态 0-正在进行中 1-成功 2-失败
     */
    private Integer code;

    /**
     * 任务信息
     */
    private String msg;

    /**
     * 字幕信息
     */
    private SubtitleObject subtitleObject;

    public static Result error() {
        return Result.builder().code(ERROR).build();
    }
}

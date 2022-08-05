package com.ulearning.video.ffmpeg.model;

import com.ulearning.video.ffmpeg.config.FfmPegConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author yangzhuo
 * @description 视频编辑类
 * @date 2022-07-29 11:01
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VideoEditRecordModel {

    /**
     * 执行状态 - 未开始
     */
    public static final Integer NOT_STARTED = 0;

    /**
     * 执行状态 - 进行中
     */
    public static final Integer STARTED = 1;

    /**
     * 执行状态 - 执行成功
     */
    public static final Integer SUCCESS = 2;

    /**
     * 执行状态 - 执行失败
     */
    public static final Integer FAIL = 3;

    /**
     * 执行状态 - 执行异常
     */
    public static final Integer ERROR = 4;

    /**
     * 主键id
     */
    private Integer id;
    /**
     * cmd命令
     */
    private String cmd;
    /**
     * 执行信息
     */
    private String msg;
    /**
     * 输出路径
     */
    private String path;
    /**
     * 执行状态
     */
    private Integer status;
    /**
     * 执行任务的机器ip
     */
    private String ip;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

    public static VideoEditRecordModel creat(String cmd, String path) {
        return VideoEditRecordModel.builder()
                .cmd(cmd).path(path).status(NOT_STARTED)
                .msg("creat")
                .ip(FfmPegConfig.IP)
                .build();
    }

    public static VideoEditRecordModel success(int id) {
        return VideoEditRecordModel.builder()
                .id(id).msg("success").status(SUCCESS)
                .build();
    }

    public static VideoEditRecordModel error(int id, String msg) {
        return VideoEditRecordModel.builder()
                .id(id).msg(msg).path("").status(ERROR)
                .build();
    }

    public static VideoEditRecordModel fail(int id, String msg) {
        return VideoEditRecordModel.builder()
                .id(id).msg(msg).path("").status(FAIL)
                .build();
    }
}

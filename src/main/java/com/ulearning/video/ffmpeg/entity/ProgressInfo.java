package com.ulearning.video.ffmpeg.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author yangzhuo
 * @description 任务进度信息类
 * @date 2022-08-02 14:42
 */
@Data
@Builder
@ApiModel("任务进度信息类")
public class ProgressInfo {
    @ApiModelProperty("任务id")
    private Integer taskId;
    @ApiModelProperty("当前等待位置")
    private Long waitIdx;
    @ApiModelProperty("正在进行的任务数")
    private Integer activeCount;
    @ApiModelProperty("总时长")
    private String totalDuration;
    @ApiModelProperty("已完成时长")
    private String duration;
    @ApiModelProperty("执行信息")
    private String msg;
    @ApiModelProperty("执行状态 [0-未开始] , [1-进行中], [2-执行成功], [3-执行失败], [4-执行异常]")
    private Integer status;
    @ApiModelProperty("输出路径")
    private String outputPath;
    @ApiModelProperty("服务中断等异常情况, 需要手动重试")
    private Boolean needRetry;
}

package com.ulearning.video.ffmpeg.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author yangzhuo
 * @description 任务信息
 * @date 2022-08-01 15:37
 */
@Data
@Builder
@ApiModel("任务信息类")
public class TaskInfo {
    @ApiModelProperty("任务id")
    private Integer taskId;
    @ApiModelProperty("正在执行的线程数")
    private Integer activeCount;
    @ApiModelProperty("核心线程数")
    private Integer corePoolSize;
    @ApiModelProperty("正在等待的任务数")
    private Long waitCount;
    @ApiModelProperty("线程池是否已满")
    private Boolean threadPoolFull;
    @ApiModelProperty("当前已完成的任务数量")
    @JsonIgnore
    private Long completedTaskCount;
    @ApiModelProperty("执行该任务的机器ip")
    @JsonIgnore
    private String ip;
}

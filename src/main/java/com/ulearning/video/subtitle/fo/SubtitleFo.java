package com.ulearning.video.subtitle.fo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author yangzhuo
 * @description 生成字幕任务表单类
 * @date 2022-09-22 9:13
 */
@Data
@ToString
@ApiModel("生成字幕任务表单类")
@EqualsAndHashCode
public class SubtitleFo {
    @ApiModelProperty("源文件 可以是视频/mp3")
    private String source;

    @ApiModelProperty("回调地址 执行完成后会返回回调信息")
    private String callback;
}

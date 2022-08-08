package com.ulearning.video.editor.fo;

import com.ulearning.video.ffmpeg.entity.VideoInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @author yangzhuo
 * @description 视频编辑表单类
 * @date 2022-07-28 11:43
 */
@Data
@ToString
@EqualsAndHashCode
@ApiModel("视频合成表单类")
public class MultipleMergeFo {
    @ApiModelProperty("编辑后的视频宽度")
    private Integer baseWidth;
    @ApiModelProperty("编辑后的视频高度")
    private Integer baseHeight;
    @ApiModelProperty("音频源下标")
    private Integer audioIndex;
    @ApiModelProperty("输出路径")
    private String outputPath;
    @ApiModelProperty("待编辑的视频源信息集合")
    private List<VideoInfo> videoInfos;
}

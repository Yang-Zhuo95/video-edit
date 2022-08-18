package com.ulearning.video.ffmpeg.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 视频信息类
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ApiModel("视频信息类")
public class VideoInfo {
    /**
     * 视频宽度
     */
    @ApiModelProperty("视频宽度")
    private Integer width;
    /**
     * 视频高度
     */
    @ApiModelProperty("视频高度")
    private Integer height;
    /**
     * 偏移x坐标
     */
    @ApiModelProperty("偏移x坐标")
    private Integer x;
    /**
     * 偏移y坐标
     */
    @ApiModelProperty("偏移y坐标")
    private Integer y;
    /**
     * 视频时长
     */
    @ApiModelProperty("视频时长")
    private Long duration;
    /**
     * 视频类型
     */
    @ApiModelProperty("视频类型")
    private String format;
    /**
     * 视频源
     */
    @ApiModelProperty("视频源")
    private String source;

    public static VideoInfo emptyInfo() {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setDuration(0L);
        return videoInfo;
    }
}

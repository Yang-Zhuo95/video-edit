package com.ulearning.video.editor.fo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author yangzhuo
 * @description 抓取图片表单类
 * @date 2022-08-05 11:45
 */
@Data
@ToString
@ApiModel("抓取图片表单类")
@EqualsAndHashCode
public class CatchPictureFo {
    /**
     * 图片宽度
     */
    @ApiModelProperty("图片宽度")
    private Integer width;
    /**
     * 图片高度
     */
    @ApiModelProperty("图片高度")
    private Integer height;
    /**
     * 抓取时间
     */
    @ApiModelProperty("抓取时间")
    private Long duration;
    /**
     * 视频源
     */
    @ApiModelProperty("视频源")
    private String source;
}

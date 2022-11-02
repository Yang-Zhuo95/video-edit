package com.ulearning.video.common.utils.subtitle.eneity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author yangzhuo
 * @description 字幕片段类
 * @date 2022-09-22 16:29
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleDetail {

    private Integer index;

    private String sentences;

    private String startTime;

    private String endTime;
}

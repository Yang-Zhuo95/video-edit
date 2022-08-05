package com.ulearning.video.ffmpeg.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 时段类
 */
@Data
@Builder
public class Period {
    private BigDecimal start;
    private BigDecimal end;
    private BigDecimal duration;
}

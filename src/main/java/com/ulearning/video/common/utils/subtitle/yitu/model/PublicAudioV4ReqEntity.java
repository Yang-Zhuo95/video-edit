package com.ulearning.video.common.utils.subtitle.yitu.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author yangzhuo
 * @description 语音转字幕请求对象
 * @date 2022-09-21 17:03
 */
@Data
@Builder
public class PublicAudioV4ReqEntity {
    /**
     * 语音地址
     */
    private String audioUrl;

    /**
     * 执行结果回调地址
     */
    private String callback;
}

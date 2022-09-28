package com.ulearning.video.common.utils.subtitle.config;

import cn.hutool.core.util.StrUtil;
import com.ulearning.video.common.utils.subtitle.AudioSubtitleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangzhuo
 * @description 依图语音转字幕配置
 * @date 2022-09-21 17:13
 */
@Configuration
@ConditionalOnClass(AudioSubtitleService.class)
public class YiTuConfig {

    @Value("${subtitle.yitu.url:#{null}}")
    public String serviceUrl;

    @Value("${subtitle.yitu.accessId:#{null}}")
    public String accessId;

    @Value("${subtitle.yitu.accessKey:#{null}}")
    public String accessKey;

    public boolean configured() {
        return StrUtil.isNotBlank(serviceUrl)
                && StrUtil.isNotBlank(accessId)
                && StrUtil.isNotBlank(accessKey);
    }
}

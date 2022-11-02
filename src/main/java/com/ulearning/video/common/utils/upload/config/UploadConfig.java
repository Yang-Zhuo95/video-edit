package com.ulearning.video.common.utils.upload.config;

import com.ulearning.video.common.utils.upload.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangzhuo
 * @description 基础配置
 * @date 2022-09-20 10:05
 */
@Configuration
@ConditionalOnClass(UploadService.class)
public class UploadConfig {
    /**
     * 默认使用此服务
     */
    @Value("${upload.server}")
    public String server;

}

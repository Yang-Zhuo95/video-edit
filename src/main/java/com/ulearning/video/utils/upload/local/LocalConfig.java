package com.ulearning.video.utils.upload.local;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangzhuo
 * @description 本地上传配置
 * @date 2022-09-19 16:30
 */
@Configuration
public class LocalConfig {
    public static String filePath;
    public static String localDownloadUrl;

    @Value("${recordVideo.basePath:''}")
    public void setFilePath(String value) {
        filePath = value;
    }

    @Value("${recordVideo.path:''}")
    public void setLocalDownloadUrl(String value) {
        localDownloadUrl = value;
    }

    public static boolean configured() {
        return StrUtil.isNotBlank(filePath)
                && StrUtil.isNotBlank(localDownloadUrl);
    }
}

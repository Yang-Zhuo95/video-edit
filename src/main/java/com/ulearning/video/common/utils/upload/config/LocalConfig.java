package com.ulearning.video.common.utils.upload.config;

import cn.hutool.core.util.StrUtil;
import com.ulearning.video.common.utils.upload.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangzhuo
 * @description 本地上传配置
 * @date 2022-09-19 16:30
 */
@Configuration
@ConditionalOnClass(UploadService.class)
public class LocalConfig {

    @Value("${recordVideo.basePath:#{null}}")
    public String filePath;

    @Value("${recordVideo.path:#{null}}")
    public String localDownloadUrl;


    public boolean configured() {
        return StrUtil.isNotBlank(filePath)
                && StrUtil.isNotBlank(localDownloadUrl);
    }
}

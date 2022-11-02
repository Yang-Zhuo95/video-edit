package com.ulearning.video.common.utils.upload.config;

import cn.hutool.core.util.StrUtil;
import com.ulearning.video.common.utils.upload.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangzhuo
 * @description 七牛上传配置
 * @date 2022-09-19 17:06
 */
@Configuration
@ConditionalOnClass(UploadService.class)
public class QiniuConfig {

    @Value("${qiniu.accesskey:#{null}}")
    public String accessKey;
    @Value("${qiniu.secretkey:#{null}}")
    public String secretkey;
    @Value("${qiniu.bucket:#{null}}")
    public String bucket;
    @Value("${qiniu.domain:#{null}}")
    public String domain;
    @Value("${qiniu.pipeline:#{null}}")
    public String pipeline;
    @Value("${qiniu.download:#{null}}")
    public String download;
    @Value("${qiniu.virtualpath:#{null}}")
    public String virtualpath;

    public boolean configured() {
        return StrUtil.isNotBlank(accessKey)
                && StrUtil.isNotBlank(secretkey)
                && StrUtil.isNotBlank(bucket)
                && StrUtil.isNotBlank(domain)
                && StrUtil.isNotBlank(pipeline)
                && StrUtil.isNotBlank(download)
                && StrUtil.isNotBlank(virtualpath);
    }
}

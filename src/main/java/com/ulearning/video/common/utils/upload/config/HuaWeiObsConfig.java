package com.ulearning.video.common.utils.upload.config;

import cn.hutool.core.util.StrUtil;
import com.ulearning.video.common.utils.upload.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @author yangzhuo
 * @description 华为上传配置类
 * @date 2022-09-19 16:16
 */
@Configuration
@ConditionalOnClass(UploadService.class)
public class HuaWeiObsConfig {

    @Value("${huaweiCloud.ak:#{null}}")
    public String ak;

    @Value("${huaweiCloud.sk:#{null}}")
    public String sk;

    @Value("${huaweiCloud.obs.bucket:#{null}}")
    public String bucket;

    @Value("${huaweiCloud.obs.endPoint:#{null}}")
    public String endPoint;

    @Value("${huaweiCloud.obs.region:#{null}}")
    public String regionId;

    @Value("${huaweiCloud.obs.template.mp3:#{null}}")
    public Integer templateMp3;

    @Value("${huaweiCloud.obs.template.mp4:#{null}}")
    public Integer templateMp4;

    @Value("${huaweiCloud.obs.urldomain:#{null}}")
    public String domain;

    @Value("${huaweiCloud.obs.template.m3u8:#{null}}")
    public Integer templateM3u8;

    @Value("${huaweiCloud.obs.virtualpath:#{null}}")
    public String virtualPath;

    public boolean configured() {
        return StrUtil.isNotBlank(ak)
                && StrUtil.isNotBlank(sk)
                && StrUtil.isNotBlank(bucket)
                && StrUtil.isNotBlank(endPoint)
                && StrUtil.isNotBlank(regionId)
                && Objects.nonNull(templateMp3)
                && Objects.nonNull(templateMp4)
                && Objects.nonNull(domain)
                && Objects.nonNull(templateM3u8)
                && Objects.nonNull(virtualPath);
    }
}

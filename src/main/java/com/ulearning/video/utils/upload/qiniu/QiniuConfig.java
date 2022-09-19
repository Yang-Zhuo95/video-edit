package com.ulearning.video.utils.upload.qiniu;

import cn.hutool.core.util.StrUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author yangzhuo
 * @description 七牛上传配置
 * @date 2022-09-19 17:06
 */
@Configuration
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfig {

    public static String ACCESSKEY;
    public static String SECRETKEY;
    public static String BUCKET;
    public static String DOMAIN;
    public static String PIPELINE;
    public static String DOWNLOAD;
    public static String VIRTUALPATH;

    private String accessKey;
    private String secretkey;
    private String bucket;
    private String domain;
    private String pipeline;
    private String download;
    private String virtualpath;

    @PostConstruct
    private void configValues() {
        ACCESSKEY = this.accessKey;
        SECRETKEY = this.secretkey;
        BUCKET = this.bucket;
        DOMAIN = this.domain;
        PIPELINE = this.pipeline;
        DOWNLOAD = this.download;
        VIRTUALPATH = this.virtualpath;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public void setVirtualpath(String virtualpath) {
        this.virtualpath = virtualpath;
    }

    public static boolean configured() {
        return StrUtil.isNotBlank(BUCKET)
                && StrUtil.isNotBlank(DOMAIN)
                && StrUtil.isNotBlank(ACCESSKEY)
                && StrUtil.isNotBlank(SECRETKEY)
                && StrUtil.isNotBlank(PIPELINE)
                && StrUtil.isNotBlank(DOWNLOAD)
                && StrUtil.isNotBlank(VIRTUALPATH);
    }
}

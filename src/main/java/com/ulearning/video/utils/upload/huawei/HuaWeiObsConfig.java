package com.ulearning.video.utils.upload.huawei;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangzhuo
 * @description 华为上传配置类
 * @date 2022-09-19 16:16
 */
@Configuration
public class HuaWeiObsConfig {

    public static String ak;

    public static String sk;

    public static String bucket;

    public static String endPoint;

    @Value("${huaweiCloud.ak:''}")
    public void setAk(String ak) {
        HuaWeiObsConfig.ak = ak;
    }

    @Value("${huaweiCloud.sk:''}")
    public void setSk(String sk) {
        HuaWeiObsConfig.sk = sk;
    }

    @Value("${huaweiCloud.obs.bucket:''}")
    public void setBucket(String bucket) {
        HuaWeiObsConfig.bucket = bucket;
    }

    @Value("${huaweiCloud.obs.endPoint:''}")
    public void setEndPoint(String endPoint) {
        HuaWeiObsConfig.endPoint = endPoint;
    }

    public static boolean configured() {
        return StrUtil.isNotBlank(ak)
                && StrUtil.isNotBlank(sk)
                && StrUtil.isNotBlank(bucket)
                && StrUtil.isNotBlank(endPoint);
    }
}

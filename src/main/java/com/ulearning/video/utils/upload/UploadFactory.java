package com.ulearning.video.utils.upload;

import cn.hutool.core.util.StrUtil;
import com.ulearning.video.utils.upload.qiniu.QiniuConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author yangzhuo
 * @description 上传工具工厂
 * @date 2021-10-11 10:34
 */
@Component
public class UploadFactory {

    // 默认使用此服务
    private static String server;

    @Value("${upload.server:''}")
    public void setServer(String value) {
        server = value;
    }

    private static Map<String, UploadService> serviceMap = new HashMap<>();

    @PostConstruct
    private void init() {
        ServiceLoader<UploadService> services = ServiceLoader.load(UploadService.class);
        for (UploadService service : services) {
            if (StrUtil.isNotBlank(service.serverName())) {
                serviceMap.put(service.serverName(), service);
            }
        }
    }

    public static UploadService getInstance() {
        return getInstance(server);
    }

    public static UploadService getInstance(String server) {
        return serviceMap.get(server);
    }

}

package com.ulearning.video.common.utils.upload;

import com.ulearning.video.common.utils.upload.config.UploadConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yangzhuo
 * @description 上传工具静态工厂
 * @date 2021-10-11 10:34
 */
@Slf4j
@Component
public class UploadFactory {

    @Autowired
    private List<UploadService> services;

    @Autowired
    private UploadConfig uploadConfig;

    /**
     * 默认使用此服务
     */
    private static String server;

    private static Map<String, UploadService> serviceMap = new HashMap<>();

    @PostConstruct
    private void init() {
        log.info("初始化上传工厂");
        server = uploadConfig.server;
        serviceMap = services.stream().filter(UploadService::configured)
                .collect(Collectors.toMap(UploadService::serverName, Function.identity(), (v1, v2) -> v1));
    }

    public static UploadService getInstance() {
        return getInstance(server);
    }

    public static UploadService getInstance(String server) {
        return serviceMap.get(server);
    }

}

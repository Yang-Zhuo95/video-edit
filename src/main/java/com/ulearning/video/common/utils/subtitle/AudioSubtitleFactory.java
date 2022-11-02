package com.ulearning.video.common.utils.subtitle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yangzhuo
 * @description 语音转字幕静态工厂
 * @date 2022-09-21 17:43
 */
@Slf4j
@Component
public class AudioSubtitleFactory {

    private static String defaultService;

    @Value("${subtitle.service.default}")
    public void setDefaultService(String defaultService) {
        AudioSubtitleFactory.defaultService = defaultService;
    }

    @Autowired
    private List<AudioSubtitleService> services;

    private static Map<String, AudioSubtitleService> serviceMap = new HashMap<>();

    @PostConstruct
    private void init() {
        log.info("初始化语音转字幕工厂");
        serviceMap = services.stream().filter(AudioSubtitleService::configured)
                .collect(Collectors.toMap(AudioSubtitleService::serverName, Function.identity(), (v1, v2) -> v1));
    }

    public static AudioSubtitleService getInstance() {
        return getInstance(defaultService);
    }

    public static AudioSubtitleService getInstance(String service) {
        return serviceMap.get(service);
    }

}

package com.ulearning.video.security;

import com.ulearning.video.security.interceptor.I18ninterceptor;
import com.ulearning.video.security.interceptor.ServiceInterceptor;
import com.ulearning.video.security.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 类说明:域名地址过滤
 * @author hlz
 * @ClassName: WebMvcConfig
 * @date 2017年12月22日 下午3:34:43
 * <p>
 * Copyright (c) 2006-2017.Beijing WenHua Online Sci-Tech Development Co. Ltd
 * All rights reserved.
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Resource
    private TokenInterceptor tokenInterceptor;

    @Resource
    private I18ninterceptor i18ninterceptor;

    @Resource
    private ServiceInterceptor serviceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration ir = registry.addInterceptor(tokenInterceptor);
        InterceptorRegistration i18 = registry.addInterceptor(i18ninterceptor);
        InterceptorRegistration si = registry.addInterceptor(serviceInterceptor);
        i18.addPathPatterns("/**");
        ir.addPathPatterns("/**")
                .excludePathPatterns(//不需要token校验的接口
                );
        si.addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*")
                .maxAge(31536000);//一年
    }

    /**
     * 解决中文乱码
     */
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }


    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解决静态资源无法访问
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        // 解决swagger无法访问
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        // 解决swagger的js文件无法访问
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 解决swagger-bootstrap-ui无法访问
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");


    }
}
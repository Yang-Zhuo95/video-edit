package com.ulearning.video.security.interceptor;

import com.ulearning.video.ffmpeg.executor.FfmPegExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yangzhuo
 * @description 服务拦截器
 * @date 2022-07-29 13:07
 */
@Component
public class ServiceInterceptor implements HandlerInterceptor {
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        FfmPegExecutor.clear();
    }
}

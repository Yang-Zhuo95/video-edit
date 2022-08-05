package com.ulearning.video.security.interceptor;

import com.ulearning.video.utils.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

/**
 * 国际化
 *
 * @Author comemory
 * @company 北京文华在线-ulearning
 * @Date 2020/3/3013:57
 */
@Component
public class I18ninterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o) throws Exception {
    	String language=null;
        if(StringUtil.valid(request.getParameter("lang"))){
            language = request.getParameter("lang");
        }
        if(language==null) {
            String header = request.getHeader("Accept-Language");
            if (StringUtil.valid(header)) {
                List<String> headers = StringUtil.split(header, ";");
                if (null != headers && !headers.isEmpty()) {
                    language = headers.get(0);
                }
            }
        }
        request.setAttribute("locale_language",getLocale(language));
        return true;
    }
    //根据language 获取Locale
    private Locale getLocale(String language){
        Locale locale = new Locale("zh", "CN");
        if(language!=null && language.contains("en")){
            locale = new Locale("en", "US");
        }
        if(language!=null && language.contains("tw")){
            locale = new Locale("zh", "TW");
        }
        if(language!=null && language.contains("in")){
            locale = new Locale("in", "ID");
        }
        if(language!=null && language.contains("id")){
            locale = new Locale("in", "ID");
        }
        if(language!=null && language.contains("es")){
            locale = new Locale("es", "ES");
        }
        if(language!=null && language.contains("ar")){
            locale = new Locale("ar", "SA");
        }
        if(language!=null && language.contains("th")){
            locale = new Locale("th", "TH");
        }
        return locale;
    }
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}

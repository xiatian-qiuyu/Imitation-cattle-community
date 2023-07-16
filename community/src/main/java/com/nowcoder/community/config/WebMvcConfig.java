package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.DataInterceptor;
import com.nowcoder.community.controller.interceptor.LoginRequireInterceptor;
//import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import com.nowcoder.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequireInterceptor loginRequireInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;
    @Autowired
    private DataInterceptor dataInterceptor;

    /**
     * 重写addInterceptors方法，添加拦截器
     * 登录凭证拦截器
     * @param registry
     */

    //拦截器的执行顺序是按照addInterceptor的注册顺序来的
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
//        registry.addInterceptor(loginRequireInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }

}

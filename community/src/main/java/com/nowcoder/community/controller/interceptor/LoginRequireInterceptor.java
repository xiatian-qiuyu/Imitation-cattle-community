package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Component
public class LoginRequireInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    //在请求之前调用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断是否是方法
        if(handler instanceof HandlerMethod){
            //强制转换
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获取方法
            Method method = handlerMethod.getMethod();
            //获取方法上的LoginRequired注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
//            //获取方法上的所有注解
//            Annotation[] annotations = method.getAnnotations();
            //判断是否有注解
            //如果有注解，但是没有登录，就重定向到登录页面
            if(loginRequired!=null&&hostHolder.getUser()==null ){
                //request.getContextPath()用于获取当前项目的根路径
                response.sendRedirect(request.getContextPath()+"/toLogin");
                return false;
            }
        }
        return true;
    }
}

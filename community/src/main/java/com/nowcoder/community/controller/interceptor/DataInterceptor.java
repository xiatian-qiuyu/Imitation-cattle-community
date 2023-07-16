package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DataService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DataInterceptor implements HandlerInterceptor{
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private DataService dataService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //ip加入uv
        dataService.recordUV(request.getRemoteHost());
        System.out.println("ip为--->"+request.getRemoteHost());
        User user = hostHolder.getUser();
        if(user!=null){
            //userId加入dau
            dataService.recordDAU(user.getId());
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //获取当天uv
        long uv = dataService.calculateUV(new Date(),new Date());
        //获取当天dau
        long dau = dataService.calculateDAU(new Date(),new Date());
        //将uv和dau传给模板引擎
        if (modelAndView != null) {
            modelAndView.addObject("todayUV",uv);
            modelAndView.addObject("todayDAU",dau);
        }
    }
}

package com.nowcoder.community.controller.interceptor;


import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    //Controller之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取cookie
        String ticket = CookieUtil.getCookie(request, "ticket");
        if(ticket!=null){
            System.out.println("preHandle-->"+"ticket:"+ticket);
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //有效凭证
            if(loginTicket!=null&&loginTicket.getStatus()==0&&loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户
                hostHolder.setUser(user);
                //构建用户认证的结果，并存入SecurityContext，以便于Security进行授权
                Authentication authentication =new UsernamePasswordAuthenticationToken(
                    user,user.getPassword(),userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
                //请求的用户都不一样，每一次请求都会创建一个新的线程。
                System.out.println("在本次请求中持有用户-->"+user);

            }
        }
        return true;
    }

    //Controller之后,模板引擎之前执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null&&modelAndView!=null){
            System.out.println("postHandle"+"-->user:"+user+"-->"+"modelAndView"+modelAndView);
            System.out.println("loginUser--->" + user.getUsername());
            modelAndView.addObject("loginUser",user);
        }
    }

    //模板引擎之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("afterCompletion---->");
        hostHolder.clear();
    }
}


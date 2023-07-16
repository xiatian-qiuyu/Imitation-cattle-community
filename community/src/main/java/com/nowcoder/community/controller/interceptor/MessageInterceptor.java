package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.HostHolder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        //一定要判断user是否为空，否则messageService.findUnReadLettersCount(user.getId(), null)会报错，导致重定向次数过多
        //modelAndView要不为空，应为当异步请求时，modelAndView为空，会抛出nullpointerexception
        if(user!=null&&modelAndView!=null){
            int unReadLettersCount = messageService.findUnReadLettersCount(user.getId(), null);
            int noticeUnreadcount = messageService.findNoticeUnreadcount(user.getId(), null);
            int allUnreadCount = unReadLettersCount+noticeUnreadcount;
            modelAndView.addObject("allUnreadCount", allUnreadCount);
        }
    }
}
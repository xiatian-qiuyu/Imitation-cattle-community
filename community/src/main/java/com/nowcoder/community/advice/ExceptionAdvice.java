package com.nowcoder.community.advice;

import com.nowcoder.community.util.CommunityUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ControllerAdvice注解表示这是一个通知类，用于处理异常。
 * 统一异常处理类
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice{
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public void handlerException(Exception e,HttpServletRequest request,HttpServletResponse response) throws IOException {
        //获取异常信息
        logger.error("服务器异常:"+e.getMessage());
        //获取异常的堆栈信息
        for(StackTraceElement element:e.getStackTrace()){
            logger.error(element.toString());
        }
        //获取请求的方式
        String xRequestWith = request.getHeader("x-requested-with");
        //异步请求
        if("XMLHttpRequest".equals(xRequestWith)){
            response.setContentType("application/plain;charset=utf-8");
            //获取输出流
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常!"));
        }else {
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }
}

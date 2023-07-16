package com.nowcoder.community.asscept;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户[1.2.3.4],在[xxx],访问了[com.nowcoder.community.service.xxx()].
        //RequestContextHolder.getRequestAttributes()获取当前线程的请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //因为当消费者调用service是，并没有经过controller，没有request和response，所以attributes为null
        if(attributes==null){
            return;
        }
        HttpServletRequest request = (HttpServletRequest) attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //joinPoint.getSignature().getDeclaringTypeName()获取全限定名，如com.nowcoder.community.service.xxx()
        //joinPoint.getSignature()获取连接点签名信息
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));

    }


}

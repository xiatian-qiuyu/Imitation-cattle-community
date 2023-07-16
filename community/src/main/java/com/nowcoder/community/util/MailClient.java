package com.nowcoder.community.util;


import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

//发送邮件的工具类，封装了发送邮件的方法
@Component
public class MailClient {
    // 日志记录
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    @Value("${spring.mail.username}")
    private String from;
    @Autowired
    private JavaMailSender javaMailSender;
    public void sendMail(String to,String subject,String content){
        try {
            // 创建邮件
            MimeMessage message = javaMailSender.createMimeMessage();
            // 创建邮件助手
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 设置发件人
            helper.setFrom(from);
            // 设置收件人
            helper.setTo(to);
            // 设置主题
            helper.setSubject(subject);
            // 设置内容
            helper.setText(content,true);
            // 发送邮件
            javaMailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败:"+e.getMessage());
        }
    }
}


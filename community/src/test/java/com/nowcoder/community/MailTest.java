package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;

    // 引入模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void sendTextMail(){
        mailClient.sendMail("2336867661@qq.com","Test","Welcome");
    }
    @Test
    public void sendHtmlMail(){
        Context context = new Context();
        // 设置变量
        context.setVariable("username","sunday");
        // 生成html
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("2336867661@qq.com","HTML",content);

    }
}

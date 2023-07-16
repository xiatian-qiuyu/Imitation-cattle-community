package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    // 注册一个bean，这个bean就是一个kaptcha的生产者，用来生成验证码
    @Bean
    public Producer kaptchaProduce(){
        Properties properties = new Properties();
        // 设置验证码的宽度
        properties.setProperty("kaptcha.image.width", "100");
        // 设置验证码的高度
        properties.setProperty("kaptcha.image.height", "40");
        // 设置验证码的字体
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        // 设置验证码的字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        // 设置验证码的字体样式
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYAZ");
        // 设置验证码的长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 设置验证码的干扰线
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        // 生成一个验证码的实现类

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        // 生成一个配置类
        Config config = new Config(properties);
        // 将配置类传入验证码的实现类
        kaptcha.setConfig(config);
        // 返回
        return kaptcha;
    }

}

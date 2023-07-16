package com.nowcoder.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {
    private final static Logger logger = LoggerFactory.getLogger(WkConfig.class);
    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @PostConstruct
    public void init(){
        //创建WK图片目录
        File file = new File(wkImageStorage);
        //file.exists()是判断文件是否存在，file.mkdir()是创建文件夹
        if(!file.exists()){
            file.mkdir();
            logger.info("创建WK图片目录："+wkImageStorage);
        }
    }
}

//@PostConstruct注解是Spring框架提供的注解之一，用于在Bean创建完成后执行初始化方法。它所在的包为javax.annotation，但是它的实现是由Spring框架提供的。
//加上@Configuration注解后，这个WkConfig类就交给了Spring管理，成为了一个Bean，可以被其他的Bean引用和使用。

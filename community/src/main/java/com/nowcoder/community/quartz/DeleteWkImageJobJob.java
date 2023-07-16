package com.nowcoder.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * 删除1分钟之前由分享功能所创建的临时文件。
 */
public class DeleteWkImageJobJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(DeleteWkImageJobJob.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        File[] fileList = new File(wkImageStorage).listFiles();
        if(fileList != null&&fileList.length!=0) {
            for (File file:fileList){
/*                //获取文件的绝对路径
                Path path = Paths.get(file.getAbsolutePath());
                //获取文件的创建时间
                FileTime creationTime = Files.readAttributes(path, BasicFileAttributes.class).creationTime();*/
                if(System.currentTimeMillis()-file.lastModified() >=60*1000){
                    file.delete();
                    logger.info(String.format("清除文件[%s]成功",file.getName()));
                }
            }
        }else{
            logger.info("没有要清理的文件！");
        }
    }
}

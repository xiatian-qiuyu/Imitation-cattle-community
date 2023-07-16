package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * 分享
 */

@Controller
public class ShareController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);
    @Autowired
    private EventProducer eventProducer;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Value("${qiniu.bucket.share.url}")
    private String shareUrl;
    @GetMapping("/share")
    @ResponseBody
    public String share(String htmlUrl){
        String fileName = CommunityUtil.gennerateUUID();
        //异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                .setData("suffix",".png");
        eventProducer.fileEvent(event);

        HashMap<String, Object> map = new HashMap<>();
//        map.put("shareUrl",domain+contextPath+"/share/image"+"/"+fileName);
        map.put("shareUrl",shareUrl+"/"+fileName);
        return CommunityUtil.getJSONString(0,null,map);
    }

    //废弃
    //获取长图（本地）
    @GetMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response){
        if(StringUtils.isEmpty(fileName)){
            throw  new IllegalArgumentException("文件名不能为空！");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage+"/"+fileName+".png");
        try (//响应输出流
             OutputStream os = response.getOutputStream();
             //FileInputStream是文件输入流，用于从文件系统中的某个文件中获得输入字节。读取文件内容到程序中
             //FileOutputStream是文件输出流，用于将数据写入File或FileDescriptor指定的文件中
             FileInputStream fs = new FileInputStream(file);
             )
        {
            //字节缓冲区
            byte[] buffer = new byte[1024];
            int b = 0;
            //从输入流中读取字节，并将最多buffer.length个字节读入在缓冲区数组buffer中
            while((b= fs.read(buffer))!=-1){
                //指定byte数组从0开始，读取b个字节写入响应输出流
                os.write(buffer,0,b);
            }
        }catch (IOException e){
            logger.error("获取长图失败："+e.getMessage());
        }
    }
}

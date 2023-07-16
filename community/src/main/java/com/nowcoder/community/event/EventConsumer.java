package com.nowcoder.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Value("${wk.image.command}")
    private String wkImageCommand;
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    //七牛云密钥
    @Value("${qiniu.key.access}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;
    //ThreadPoolTaskScheduler是Spring提供的一个可以定时执行任务的类,
    // 通过它可以实现定时发送系统通知的功能
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    //消费评论、点赞、关注事件
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息为空！");
        }
        Event event=JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误！");
        }
        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());

        HashMap<String, Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        //将event中的data数据放到content中
        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //消费发帖事件
    @KafkaListener(topics = TOPIC_PUBLISH)
    public void handlePushMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息为空！");
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误！");
        }

        DiscussPost discussPost = null;
        if (event != null) {
            discussPost = discussPostService.findDiscussPostsById(event.getEntityId());
        }
        elasticsearchService.saveDiscussPost(discussPost);
    }

    //消费分享事件
    //生产者发送的消息都会被封装成ConsumerRecord对象
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息为空");
        }

        if (record != null) {
            //JSONObject.parseObject()方法可以将json字符串转换成java对象
            Event event  = JSONObject.parseObject(record.value().toString(),Event.class);
            if(event==null){
                logger.error("消息格式错误");
            }else{
                String htmlUrl = (String) event.getData().get("htmlUrl");
                String fileName = (String) event.getData().get("fileName");
                String suffix = (String) event.getData().get("suffix");
                //如--> d:/work/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com d:/work/data/wk-images/3.png
                String cmd = wkImageCommand + " --quality 75  " + htmlUrl + " " +wkImageStorage+"/"+ fileName + suffix;

                try {
                    Runtime.getRuntime().exec(cmd);
                    logger.info("生成长图成功：" + cmd);
                } catch (IOException e) {
                    logger.error("生成长图失败：" + e.getMessage());
                }

                //启用定时器，监视该图片，一旦生成了，则上传至七牛云
                UploadTask task = new UploadTask(fileName,suffix);
                //scheduleAtFixedRate()方法可以实现定时执行任务的功能
                //第一个参数是要执行的任务，第二个参数是任务的执行时间，第三个参数是任务的执行周期
                //返回值是一个Future对象，可以用来取消任务
                //taskScheduler.scheduleAtFixedRate
                Future future = taskScheduler.scheduleAtFixedRate(task,500);
                task.setFuture(future);
            }
        }
    }

    /**
     * 上传任务
     */

    class UploadTask implements Runnable{
        //文件名
        private String fileName;
        //文件后缀
        private String suffix;
        //启动任务的返回值
        private Future future;
        //开始时间
        private long startTime;
        //执行次数
        private int uploadTimes;

        public UploadTask(String fileName,String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }
        public void setFuture(Future future){
            this.future = future;
        }


        @Override
        public void run() {
            //生成失败
            if(System.currentTimeMillis()-startTime>30000){
                logger.error("执行时间过长，终止任务："+fileName);
                future.cancel(true);
                return;
            }
            //上传失败
            if(uploadTimes>=3){
                logger.error("上传次数过多，终止任务："+fileName);
                //取消任务,true表示立即停止任务，false表示允许任务执行完毕
                future.cancel(true);
                return;
            }
            /**
             * 上传图片到七牛云
             */
            String path = wkImageStorage+"/"+fileName+suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("开始第%d次上传[%s]",++uploadTimes,fileName));
                //设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                //设置上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                //设置上传机房,华东浙江2机房
                Configuration cfg = new Configuration(Region.huadongZheJiang2());
                UploadManager uploadManager = new UploadManager(cfg);

                try {
                    //put中的参数分别的作用是：文件路径，文件名，上传凭证，上传策略，文件类型，是否使用https
                    Response response = uploadManager.put(path, fileName,uploadToken,null,"image/"+suffix,false);
                    //解析上传成功的结果
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    if(jsonObject==null||jsonObject.get("code")==null||!jsonObject.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s]", uploadTimes, fileName));
                    }else{
                        logger.info(String.format("第%d次上传成功[%s]", uploadTimes, fileName));
                        //上传成功后不再一直执行任务，取消任务
                        future.cancel(true);
                    }
                } catch (QiniuException ex) {
                    logger.info(String.format("第%d次上传失败[%s]", uploadTimes, fileName));
                }
            }else {
                logger.info("等待图片生成["+fileName+"]");
            }
        }
    }
}


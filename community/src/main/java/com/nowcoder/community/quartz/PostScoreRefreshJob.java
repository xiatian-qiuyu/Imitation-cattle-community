package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 刷新帖子分数的任务
 */
@Component
public class PostScoreRefreshJob implements Job,CommunityConstant {
    @Autowired
    private LikeService likeService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private EventProducer eventProducer;
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    //牛客纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-5-10 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败"+e);
        }
    }

    /**
     * 任务的执行逻辑,刷新帖子分数
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        //返回 RedisTemplate 的 BoundSetOperations 类型对象，该对象支持对 Redis 的 SET 数据结构进行操作
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if(operations.size()==0){
            logger.info("任务取消，没有需要刷新的帖子！");
        }else {
            logger.info("任务开始，正在刷新帖子分数："+operations.size());
            while(operations.size()>0){
                this.refresh((int)operations.pop());
            }
            logger.info("任务结束，刷新帖子分数结束");
        }
    }

    /**
     * 刷新帖子分数
     * @param postId
     */
    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostsById(postId);
        if(post.getStatus()==2){
            //帖子被删除
            logger.error("该帖子已被删除，id："+postId);
            return;
        }
        //是否精华
        boolean prime = post.getStatus() == 1;
        //评论数
        int commentCount = post.getCommentCount();
        //点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITYTYPE_POST, postId);
        //计算权重w:精华分+评论数*10+点赞数*2
        double w = (prime ? 75 : 0)+commentCount * 10+likeCount * 2;
        //分数=帖子权重+距离天数
        double score = (Math.log10(Math.max(w,1))+ (double) (post.getCreateTime().getTime() - epoch.getTime()) /(1000 * 3600 * 24));
        //更新帖子分数
        discussPostService.updateDiscussPostScore(postId,score);
        post.setScore(score);

        //同步搜索数据
        //1.使用消息队列来同步搜索数据
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITYTYPE_POST)
                .setEntityId(postId);
        eventProducer.fileEvent(event);

        //2.直接上传到es
        /*elasticsearchService.saveDiscussPost(post);*/

        //使用消息队列来同步搜索数据，而不是直接上传到es
        //因为如果同一时刻有很多帖子需要上传到es，那么就会造成es服务器压力过大，导致es服务器崩溃
    }
}

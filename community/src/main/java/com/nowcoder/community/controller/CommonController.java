package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@RequestMapping("/comment")
@Controller
public class CommonController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 添加评论
     */
    @PostMapping("/add/{discussPostId}")
    public String addCommon(@PathVariable("discussPostId") int discussPostId, Comment comment){
        //entityId和entityType,content，targetId在前端的隐藏框设置好了
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setEntityType(comment.getEntityType())
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        //评论的是帖子
        if(comment.getEntityType()==ENTITYTYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostsById(comment.getEntityId());
            //评论的帖子的作者
            event.setEntityUserId(target.getUserId());
        }
        //评论的是评论
        if(comment.getEntityType()==ENTITYTYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fileEvent(event);

        //如果是评论帖子，评论帖子后，帖子的评论数量会变化
        //所以触发发帖事件
        if(comment.getEntityType()==ENTITYTYPE_POST){
            event = new Event();
            event.setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITYTYPE_POST)
                    .setEntityId((discussPostId))
                    .setEntityUserId(discussPostService.findDiscussPostsById(discussPostId).getUserId());
            eventProducer.fileEvent(event);
            //将帖子Id存入redis中
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }

        //回到帖子详情页面
        return "redirect:/disscussPost/detail/"+discussPostId;
    }
}

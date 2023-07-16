package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/disscussPost")
public class DisscussPostController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    private static final Logger logger = LoggerFactory.getLogger(DisscussPostController.class);
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 发布帖子
     * @param title
     * @param content
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public String pushDisscuss(String title,String content){
        if(StringUtils.isEmpty(title)||StringUtils.isEmpty(content)){
            logger.error("标题或内容为空!");
            throw new IllegalArgumentException("参数为空");
        }
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"未登录，请先登录！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        //因为status和type是int类型，所以默认值为0，不用设置
        discussPostService.addDisscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITYTYPE_POST)
                .setEntityId(post.getId())
                //设置帖子作者等其实没必要，因为我搜索的时候，只要搜索帖子，
                // 点击帖子，连接到帖子详情页，这是不是查询的ES中的数据，而是数据库中的数据，
                //所以有帖子的id就可以了，不需要作者的id
                .setEntityUserId(post.getUserId());
        eventProducer.fileEvent(event);

        //将帖子id存入redis中，用于计算分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());

        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    /**
     * 查询帖子详情
     */
    @GetMapping("/detail/{discussPostId}")
    public String detdilDiscuss(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        DiscussPost post = discussPostService.selectDiscussPostById(discussPostId);
        //1.帖子
        model.addAttribute("post",post);
        User user = userService.findUserById(post.getUserId());
        //2.作者
        model.addAttribute("user",user);
        //3.点赞
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITYTYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态，如果没登录，就是0，如果登录了才去获取点赞状态
        int likeStatus =hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITYTYPE_POST, discussPostId);
        model.addAttribute("likeStatus",likeStatus);
        //评论分页信息
        page.setRows(post.getCommentCount());
        page.setPath("/disscussPost/detail/"+discussPostId);
        page.setLimit(5);
        //4.帖子下面的评论列表
        List<Comment> commentList = commentService.findCommentByEntity(ENTITYTYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论vo列表(用户可以获取头像,回复等)
        List<Map<String,Object>> commentVoList= new ArrayList<>();
        if(commentList!=null){
            for (Comment comment:commentList){
                //评论vo
                HashMap<String, Object> commentVo = new HashMap<>();
                //1.作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                System.out.println("用户头像---->"+userService.findUserById(comment.getUserId()).getHeaderUrl());
                //2.评论
                commentVo.put("comment",comment);
                //3.点赞
                //数量
                likeCount = likeService.findEntityLikeCount(ENTITYTYPE_COMMENT, comment.getId());
                //状态
                likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITYTYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                commentVo.put("likeStatus",likeStatus);
                //回复列表(不分页)
                List<Comment> replyList = commentService.findCommentByEntity(ENTITYTYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复的vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList!=null){
                    for(Comment reply:replyList){
                        //回复的vo
                        HashMap<String, Object> replyVo = new HashMap<>();
                        //1.回复
                        replyVo.put("reply",reply);
                        //2.作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //3.点赞
                        //数量
                        likeCount = likeService.findEntityLikeCount(ENTITYTYPE_COMMENT, reply.getId());
                        //状态
                        likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITYTYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        replyVo.put("likeStatus",likeStatus);
                        //4.回复的目标
                        User target = reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                //回复数量(改评论的所有回复数量)
                int replyCount = commentService.findCountByEntity(ENTITYTYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

    //置顶,取消置顶
    @PostMapping("/toTop")
    @ResponseBody
    public String toTop(int id){
        //查询帖子类型
        int type = discussPostService.selectDiscussPostById(id).getType();
        if(type==1){
            //取消置顶
            discussPostService.updateDiscussPostType(id,0);
            type =0;
        }else {
            //置顶
            discussPostService.updateDiscussPostType(id,1);
            type = 1;
        }
        //改变后的状态
        HashMap<String, Object> map = new HashMap<>();
        map.put("type",type);
        //触发发帖事件
        Event event = new Event()
                .setUserId(hostHolder.getUser().getId())
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITYTYPE_POST)
                .setEntityId(id)
                //设置帖子作者
                .setEntityUserId(discussPostService.selectDiscussPostById(id).getUserId());
        eventProducer.fileEvent(event);
        return CommunityUtil.getJSONString(0,null,map);
    }

    //加精
    @PostMapping("/refinement")
    @ResponseBody
    public String Refinement(int id){
        //查询帖子状态
        int status = discussPostService.selectDiscussPostById(id).getStatus();
        HashMap<String, Object> map = new HashMap<>();
        if(status==1){
            //取消加精
            discussPostService.updateDiscussPostStatus(id,0);
            status = 0;
        }else {
            //加精
            discussPostService.updateDiscussPostStatus(id,1);
            status = 1;
        }
        map.put("status",status);
        //触发发帖事件
        Event event = new Event()
                .setUserId(hostHolder.getUser().getId())
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITYTYPE_POST)
                .setEntityId(id)
                .setEntityUserId(discussPostService.selectDiscussPostById(id).getUserId());
        eventProducer.fileEvent(event);

        // 在加精/取消加精的时候,需要重新计算帖子的分数，将帖子Id存入redis中
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);
        return CommunityUtil.getJSONString(0,null,map);
    }

    //删帖
    @PostMapping("/delete")
    @ResponseBody
    public String deletePost(int id){
        discussPostService.updateDiscussPostStatus(id,2);
        //触发发帖事件
        Event event = new Event()
                .setUserId(hostHolder.getUser().getId())
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITYTYPE_POST)
                .setEntityId(id)
                .setEntityUserId(discussPostService.selectDiscussPostById(id).getUserId());
        eventProducer.fileEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}

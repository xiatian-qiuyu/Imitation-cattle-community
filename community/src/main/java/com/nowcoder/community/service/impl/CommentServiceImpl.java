package com.nowcoder.community.service.impl;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService, CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;

    @Override
    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    @Override
    public int findCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    // @Transactional(isolation= Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)的作用是：
    //a:isolation= Isolation.READ_COMMITTED表示事务的隔离级别为读已提交，即一个事务可以读取到另一个事务已经提交的数据，但是不能读取到另一个事务未提交的数据
   //为什么这里要加事务？
    //a:因为这里有两个操作，一个是添加评论，一个是更新评论数量，这两个操作必须同时成功或者同时失败，所以要加事务
    //这里对应的是哪个事务读取到另一个事务已经提交的数据？
    //a:对应的是更新评论数量，因为更新评论数量是在添加评论之后进行的，所以这里的事务可以读取到另一个事务已经提交的数据
    @Transactional(isolation= Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Override
    public int addComment(Comment comment) {
        if(comment==null){
            throw new IllegalArgumentException("参数为空！");
        }
        //Html转义
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //添加评论
        int rows = commentMapper.insertComment(comment);
        //更新评论数量
        if(comment.getEntityType()==ENTITYTYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            //在common表中添加entityId和entityType
            //entityId可以是common的id，也可以是post的id，表示评论的是哪个具体的实体
            //entityType表示评论的是帖子还是评论
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    @Override
    public List<Comment> findUserComments(int userId, int offset, int limit) {
        return commentMapper.findUserComments(userId, offset, limit);
    }

    @Override
    public int findUserCommentCount(int userId) {
        return commentMapper.findUserCommentCount(userId);
    }

    @Override
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}

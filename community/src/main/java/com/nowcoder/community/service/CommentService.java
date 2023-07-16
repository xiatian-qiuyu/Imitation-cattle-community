package com.nowcoder.community.service;

import com.nowcoder.community.entity.Comment;

import java.util.List;


public interface CommentService {
    /**
     * 分页查询评论
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 查询总记录数
     */
    int findCountByEntity(int entityType,int entityId);

    /**
     * 添加评论
     */
    int addComment(Comment comment);

    /**
     *根据用户id查询对应用户的回复列表
     */
    List<Comment> findUserComments(int userId,int offset,int limit);

    /**
     *查询用户的回复数量
     */
    int findUserCommentCount(int userId);

    Comment findCommentById(int id);
}

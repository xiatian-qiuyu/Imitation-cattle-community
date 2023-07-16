package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CommentMapper {
    /**
     * 分页查询评论
     */
    List<Comment> selectCommentByEntity(int entityType,int entityId,int offset,int limit);

    /**
     * 查询总记录数
     */
    int selectCountByEntity(int entityType,int entityId);

    /**
     * 添加评论
     */
    int insertComment(Comment comment);

    /**
     *根据用户id查询对应用户的回复列表
     */
    List<Comment> findUserComments(int userId,int offset,int limit);

    /**
     *查询用户的回复数量
     */
    int findUserCommentCount(int userId);

    Comment selectCommentById(int id);
}

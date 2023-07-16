package com.nowcoder.community.service;

public interface LikeService {
    //点赞，支持帖子点赞，评论点赞
    void like(int userId,int entityType,int entityId,int entityUserId);

    //获取实体的点赞数量
    long findEntityLikeCount(int entityType, int entityId);

    //查询某人对某实体的点赞状态
    int findEntityLikeStatus(int userId,int entityType,int entityId);

    //获取某个用户的点赞
    int findUserLikeCount(int entityUserId);


}

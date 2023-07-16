package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService {
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode);
    int findDisscussPostRow(int userId);

    int addDisscussPost(DiscussPost discussPost);
    ////查询帖子详情
    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int eneiyId, int commentCount);

    DiscussPost findDiscussPostsById(int id);

    //修改帖子类型
    int updateDiscussPostType(int id,int type);
    //修改帖子状态
    int updateDiscussPostStatus(int id,int status);

    void updateDiscussPostScore(int id, double score);
}

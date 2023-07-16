package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface DiscussPostMapper {

    // 分页查询
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDisscussPostRow(@Param("userId") int userId);

    //插入帖子
    int inseretDiscussPost(DiscussPost discussPost);

    ////查询帖子详情
    DiscussPost selectDiscussPostById(int id);
    //
    int updateCommentCount(int id, int commentCount);

    //修改帖子类型
    int updateDiscussPostType(int id,int type);
    //修改帖子状态
    int updateDiscussPostStatus(int id,int status);

    void updateDiscussPostScore(int id, double score);
}

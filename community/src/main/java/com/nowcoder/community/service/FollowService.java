package com.nowcoder.community.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface FollowService {
    //关注,取消关注
    void follow(int userId,int entityType,int entityId);

    //查询关注的实体的数量
    long findFolloweeCount(int userId,int entityType);

    //查询某实体的粉丝数量
    long findFollowerCount(int entityType,int entityId);

    //查询当前用户是否关注该实体
    boolean followStatus(int userId,int entityType,int entityId);

    //查询某用户关注的人
    List<Map<String,Object>> findFollowees(int userId,int offset,int limit);

    //查询某用户的粉丝
    List<Map<String,Object>> findFollowers(int userId,int offset,int limit);

}

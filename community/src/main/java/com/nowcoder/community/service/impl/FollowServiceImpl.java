package com.nowcoder.community.service.impl;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.nowcoder.community.util.CommunityConstant.ENTITYTYPE_USER;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    //关注，取消关注，可以关注用户，帖子...
    @Override
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey  = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey  = RedisKeyUtil.getFollowerKey(entityType, entityId);
                //判断zset（）的key中是否有某个值的方法:zscore(key,value),如果有就返回分数，没有就返回nil
                boolean isMember = operations.opsForZSet().score(followeeKey, entityId) != null;
                redisTemplate.multi();
                if (isMember){
                    //已经关注过了，取消关注,同时将当前用户从对象粉丝列表中移除
                    operations.opsForZSet().remove(followeeKey,entityId);
                    operations.opsForZSet().remove(followerKey,userId);
                }else {
                    operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                    operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                }
                return redisTemplate.exec();
            }
        });
    }

    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        //
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    @Override
    public boolean followStatus(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }

    @Override
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITYTYPE_USER);
        //获取指定有序集合 followeeKey 的元素
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if(targetIds!=null||targetIds.isEmpty()==false){
            List<Map<String,Object>> list = new ArrayList<>();
            for(Integer targetId:targetIds){
                Map<String,Object> map = new HashMap<>();
                User user = userService.findUserById(targetId);
                map.put("user",user);
                Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
                map.put("followTime",new Date(score.longValue()));
                list.add(map);
            }
            return list;
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITYTYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if(targetIds!=null||targetIds.isEmpty()==false){
            List<Map<String, Object>> list = new ArrayList<>();
            for(Integer targetId:targetIds){
                HashMap<String, Object> map = new HashMap<>();
                User user = userService.findUserById(targetId);
                map.put("user",user);
                Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
                map.put("followTime",new Date(score.longValue()));
                list.add(map);
            }
            return list;
        }
        return null;
    }
}

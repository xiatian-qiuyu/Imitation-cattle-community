package com.nowcoder.community.service.impl;

import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞，取消点赞,支持帖子点赞，评论点赞
     * @param userId
     * @param entityType
     * @param entityId
     */
    @Override
    public void like(int userId, int entityType, int entityId,int entityUserId) {
//        //构造redis的key
//       String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if(isMember){
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }else{
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikekey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                operations.multi();
                if(isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikekey);
                }else{
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikekey);
                }
                return operations.exec();
            }
        });
    }

    /**
     * 获取实体的点赞数量
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对某实体的点赞状态,1:已赞 2：未赞
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId)?1:0;
    }

    @Override
    public int findUserLikeCount(int entityUserId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count.intValue();
    }
}

package com.iknow.community.service;

import com.iknow.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like(int userId,int entityType,int entityId,int entityUserId){
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        System.out.println(entityLikeKey);
//        // 查询是否已经点过赞
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
//        if (isMember){
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }else{
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }
        // 在对贴子或者回复点赞的同时，对个人的赞也相应的增加或者减少
        redisTemplate.execute(new SessionCallback() {
            @Nullable
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
               String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
               String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

               boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey,userId);
               redisOperations.multi();

               if (isMember){
                redisOperations.opsForSet().remove(entityLikeKey,userId);
                redisOperations.opsForValue().decrement(userLikeKey);
               }else{
                   redisOperations.opsForSet().add(entityLikeKey,userId);
                   redisOperations.opsForValue().increment(userLikeKey);
               }
                return redisOperations.exec();
            }
        });


    }

    // 查询某个实体（帖子）点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态，即是否已经点赞
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)? 1:0;
    }

    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer likeCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return likeCount == null ? 0:likeCount.intValue();
    }
}

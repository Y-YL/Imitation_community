package com.iknow.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST_SCORE = "post";
    private static final String PREFIX_RESETPASSWORD_KAPTCHA = "resetPwd";

    // 某个实体的赞， 某个帖子的赞
    // key的格式 like:entity:entityType:entityId ,存储格式 set(userId)
    // 返回redisKey
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
    // 某个用户关注的实体
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWE + SPLIT + userId +SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId ->zset (userId, now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 验证码的Rediskey
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证（loginTicket）的RedisKey
    public static String getLoginTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 获取用户的key
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    // 获取单日UV的key
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVkey(String startDate,String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date){
        return PREFIX_DAU + date;
    }
    // 区间活跃用户
    public static String getDAUKey(String startDate,String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //
    public static String getPostScore(){
        return PREFIX_POST_SCORE + SPLIT + "score";
    }

    public static String getResetpasswordKaptcha(String email){
        return PREFIX_KAPTCHA+SPLIT+PREFIX_RESETPASSWORD_KAPTCHA+SPLIT+email;
    }
}

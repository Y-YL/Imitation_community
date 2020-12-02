package com.iknow.community.service;

import com.iknow.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 将指定IP计入UV
    public void recordUV(String ip){
        String uvKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey,ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date start,Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("统计UV日期参数不能为空!");
        }
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        // 合并这些单日UV数据
        String redisKey = RedisKeyUtil.getUVkey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());

        // 返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户计入DAU
    public void recordDAU(int userId){
        String dauKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey,userId,true);
    }

    // 统计指定日期范围内的DAU
    public long calculateDAU(Date start,Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("统计DAU日期参数不能为空");
        }
        // 整理日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String dauKey = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(dauKey.getBytes());
            calendar.add(Calendar.DATE,1);
        }

        // 进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String dauKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        dauKey.getBytes(),keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(dauKey.getBytes());
            }
        });
    }



}

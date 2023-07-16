package com.nowcoder.community.service.impl;

import com.nowcoder.community.service.DataService;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {
    @Autowired
    private RedisTemplate redisTemplate;
    private SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");

    //将ip加入UV
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVkey(sf.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    @Override
    public long calculateUV(Date start, Date end) {
        if(start==null||end==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //如果开始日期晚于结束日期
        if(start.after(end)){
            throw new IllegalArgumentException("开始日期不能晚于结束日期!");
        }
        //整理该日期范围内的key
        List<String> list = new ArrayList<>();
        //获取系统当前的日历对象
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);


        //遍历日期范围内的所有日期
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVkey(sf.format(calendar.getTime()));
            list.add(key);
            //如果开始时间等于结束时间，跳出循环
            if(calendar.getTime().equals(end)){
                break;
            }
            //每次循环加一天
            calendar.add(Calendar.DATE,1);
        }
        //合并这些数据
        String redisKey = RedisKeyUtil.getUVkey(sf.format(start),sf.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,list.toArray());
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    //将UserId加入DAU
    @Override
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUkey(sf.format(new Date()));
        //把第userId位设置为true
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }

    @Override
    public long calculateDAU(Date start, Date end) {
        if(start==null||end==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(start.after(end)){
            throw new IllegalArgumentException("开始日期不能晚于结束日期!");
        }
        //整理该日期范围内的key的对应byte数组
        List<byte[]> list = new ArrayList<>();
        //获取当前系统的日历对象
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key =RedisKeyUtil.getDAUkey(sf.format(calendar.getTime()));
            list.add(key.getBytes());
            //如果开始时间等于结束时间，跳出循环
            if(calendar.getTime().equals(end)){
                break;
            }
            //每次循环加一天
            calendar.add(Calendar.DATE,1);
        }
        //进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                //合并这些数据
                String redisKey = RedisKeyUtil.getDAUkey(sf.format(start),sf.format(end));
                //因为bitOp的key参数是一个一维byte数组，而or操作的对象是对多个byte数组进行操作，所以需要将list转换为二维byte数组
                connection.bitOp(RedisStringCommands.BitOperation.OR,redisKey.getBytes(),list.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}

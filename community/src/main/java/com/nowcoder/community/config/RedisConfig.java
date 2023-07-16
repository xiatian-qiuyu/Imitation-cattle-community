package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis的配置类
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //1.设置key序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        //2.设置value序列化方式
        redisTemplate.setValueSerializer(RedisSerializer.json());
        //3.设置hash的key序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        //4.设置hash的value的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        //redisTemplate.afterPropertiesSet(),是为了让上面的配置生效
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
//为什么value的序列化方式使用json？那我后面还可以operations.opsForValue().increment(userLikekey)吗？json不是字符串吗？怎么能加1呢？怎么能加1呢？
//因为RedisSerializer.json()是一个序列化器，它可以把对象序列化成json字符串，也可以把json字符串反序列化成对象。所以，我们可以把数字1序列化成字符串"1"，然后再反序列化成数字1，这样就可以加1了。
package com.authorizer.infrastructure.config;

import com.authorizer.infrastructure.filter.ConcurrentCacheControl;
import com.authorizer.infrastructure.filter.ConcurrentTransactionFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public  class ConcurrentTransactionConfig {

    @Value("${espoc.idempotency.paths}")
    private List<String> idempotencyApiPaths;

    @Value("${espoc.idempotency.ttlInMinutes}")
    private Long ttlInMinutes;

    @Bean
    RedisTemplate<String, ConcurrentCacheControl> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisSerializer stringRedisSerializer  =  new  StringRedisSerializer ();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer  =
                new  Jackson2JsonRedisSerializer (ConcurrentCacheControl.class);

        RedisTemplate<String, ConcurrentCacheControl> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);

        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        return template;
    }

    /**
     * Obs: Make sure the idempotency filter is after all authentication related filter
     */
    @Bean
    public FilterRegistrationBean<ConcurrentTransactionFilter> idempotenceFilterRegistrationBean (
            RedisTemplate<String, ConcurrentCacheControl> redisTemplate) {

        FilterRegistrationBean<ConcurrentTransactionFilter> registrationBean = new FilterRegistrationBean();

        ConcurrentTransactionFilter concurrentTransactionFilter =  new ConcurrentTransactionFilter(redisTemplate, ttlInMinutes);

        registrationBean.setFilter(concurrentTransactionFilter);
        registrationBean.addUrlPatterns(idempotencyApiPaths.toArray(String[]:: new ));
        registrationBean.setOrder( 1 );
        return registrationBean;
    }

}

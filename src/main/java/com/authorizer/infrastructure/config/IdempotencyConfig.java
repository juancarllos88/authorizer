package com.authorizer.infrastructure.config;

import com.authorizer.infrastructure.filter.IdempotenceFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public  class  IdempotencyConfig {

    @Value("${espoc.idempotency.paths}")
    private List<String> idempotencyApiPaths;

    @Value("${espoc.idempotency.ttlInMinutes:60}")
    private Long ttlInMinutes;

    @Bean
    RedisTemplate<String, IdempotenceFilter.IdempotencyValue> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisSerializer stringRedisSerializer  =  new  StringRedisSerializer ();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer  =
                new  Jackson2JsonRedisSerializer (IdempotenceFilter.IdempotencyValue.class);

        RedisTemplate<String, IdempotenceFilter.IdempotencyValue> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);

        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        return template;
    }

    @Bean
    public FilterRegistrationBean<IdempotenceFilter> idempotenceFilterRegistrationBean (
            RedisTemplate<String, IdempotenceFilter.IdempotencyValue> redisTemplate) {

        FilterRegistrationBean<IdempotenceFilter> registrationBean = new FilterRegistrationBean();

        IdempotenceFilter  idempotenceFilter  =  new  IdempotenceFilter (redisTemplate, ttlInMinutes);

        registrationBean.setFilter(idempotenceFilter);
        registrationBean.addUrlPatterns(idempotencyApiPaths.toArray(String[]:: new ));
        registrationBean.setOrder( 1 ); //certifique-se de que o filtro de idempotência esteja após todo o filtro relacionado à autenticação
        return registrationBean;
    }

}

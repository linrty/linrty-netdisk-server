package top.linrty.netdisk.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.linrty.netdisk.common.util.RedisLock;
import top.linrty.netdisk.common.util.RedisUtil;

@Configuration
public class RedisConfig {
    @Bean
    public RedisLock redisLock(){
        return new RedisLock();
    }
    @Bean
    public RedisUtil redisUtil(){
        return new RedisUtil();
    }
}

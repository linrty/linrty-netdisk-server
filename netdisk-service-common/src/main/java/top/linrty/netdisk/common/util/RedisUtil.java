package top.linrty.netdisk.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value) {
        this.stringRedisTemplate.opsForValue().set(key, value);
    }

    public String getObject(String key) {
        return (String)this.stringRedisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value, long time) {
        if (time > 0L) {
            this.stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            this.stringRedisTemplate.opsForValue().set(key, value);
        }

    }

    public boolean hasKey(String key) {
        return this.stringRedisTemplate.hasKey(key);
    }

    public void deleteKey(String key) {
        this.stringRedisTemplate.delete(key);
    }

    public Long getIncr(String key) {
        return this.stringRedisTemplate.opsForValue().increment(key, 1L);
    }
}

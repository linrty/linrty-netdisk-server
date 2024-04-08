package top.linrty.netdisk.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisLock {
    private static final int DEFAULT_ACQUIRE_RESOLUTION_MILLIS = 100;
    private static final String UNLOCK_LUA = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then     return redis.call(\"del\",KEYS[1]) else     return 0 end ";
    private static final long LOCK_EXPIRE_TIME = 300L;

    @Resource
    StringRedisTemplate stringRedisTemplate;
    private final ThreadLocal<Map<String, LockVO>> lockMap = new ThreadLocal();

    public void lock(final String key) {
        try {
            this.acquireLock(key, 300L, -1L);
        } catch (Exception var3) {
            throw new RuntimeException("acquire lock exception", var3);
        }
    }

    public void unlock(String key) {
        try {
            this.release(key);
        } catch (Exception var3) {
            throw new RuntimeException("release lock exception", var3);
        }
    }

    public boolean tryLock(final String key) {
        try {
            return this.acquireLock(key, 300L, -1L);
        } catch (Exception var3) {
            throw new RuntimeException("acquire lock exception", var3);
        }
    }

    public boolean tryLock(String key, long time, TimeUnit unit) {
        try {
            return this.acquireLock(key, 300L, unit.toSeconds(time));
        } catch (Exception var6) {
            throw new RuntimeException("acquire lock exception", var6);
        }
    }

    private boolean acquireLock(String key, long expire, long waitTime) throws InterruptedException {
        boolean acquired = this.acquired(key);
        if (acquired) {
            return true;
        } else {
            long acquireTime = waitTime == -1L ? -1L : waitTime * 1000L + System.currentTimeMillis();
            synchronized(key.intern()) {
                String lockId = UUID.randomUUID().toString();

                while(true) {
                    long before = System.currentTimeMillis();
                    boolean hasLock = this.tryLock(key, expire, lockId);
                    if (hasLock) {
                        long after = System.currentTimeMillis();
                        Map<String, LockVO> map = (Map)this.lockMap.get();
                        if (map == null) {
                            map = new HashMap(2);
                            this.lockMap.set(map);
                        }

                        ((Map)map).put(key, new LockVO(1, lockId, expire * 1000L + before, expire * 1000L + after));
                        log.debug("acquire lock {} {} ", key, 1);
                        return true;
                    }

                    Thread.sleep(100L);
                    if (acquireTime != -1L && acquireTime <= System.currentTimeMillis()) {
                        break;
                    }
                }
            }

            log.info("acquire lock {} fail，because timeout ", key);
            return false;
        }
    }

    private void release(String key) {
        Map<String, LockVO> map = (Map)this.lockMap.get();
        if (map != null && map.size() != 0 && map.containsKey(key)) {
            LockVO vo = (LockVO)map.get(key);
            if (vo.afterExpireTime < System.currentTimeMillis()) {
                log.debug("release lock {}, because timeout ", key);
                map.remove(key);
            } else {
                int after = --vo.count;
                log.debug("release lock {} {} ", key, after);
                if (after <= 0) {
                    map.remove(key);
                    RedisCallback<Boolean> callback = (connection) -> {
                        return (Boolean)connection.eval(UNLOCK_LUA.getBytes(StandardCharsets.UTF_8), ReturnType.BOOLEAN, 1, new byte[][]{key.getBytes(StandardCharsets.UTF_8), vo.lockId.getBytes(StandardCharsets.UTF_8)});
                    };
                    this.stringRedisTemplate.execute(callback);
                }
            }
        }
    }

    private boolean tryLock(String key, long expire, String lockId) {
        try {
            RedisCallback<Boolean> callback = (connection) -> {
                return connection.set(key.getBytes(StandardCharsets.UTF_8), lockId.getBytes(StandardCharsets.UTF_8), Expiration.seconds(expire), RedisStringCommands.SetOption.SET_IF_ABSENT);
            };
            return (Boolean)this.stringRedisTemplate.execute(callback);
        } catch (Exception var6) {
            log.error("redis lock error.", var6);
            return false;
        }
    }

    private boolean acquired(String key) {
        Map<String, LockVO> map = (Map)this.lockMap.get();
        if (map != null && map.size() != 0 && map.containsKey(key)) {
            LockVO vo = (LockVO)map.get(key);
            if (vo.beforeExpireTime < System.currentTimeMillis()) {
                log.debug("lock {} maybe release, because timeout ", key);
                return false;
            } else {
                int after = ++vo.count;
                log.debug("acquire lock {} {} ", key, after);
                return true;
            }
        } else {
            return false;
        }
    }

    private static class LockVO {
        private int count;
        private String lockId;
        private long beforeExpireTime;
        private long afterExpireTime;

        LockVO(int count, String lockId, long beforeExpireTime, long afterExpireTime) {
            this.count = count;
            this.lockId = lockId;
            this.beforeExpireTime = beforeExpireTime;
            this.afterExpireTime = afterExpireTime;
        }
    }
}

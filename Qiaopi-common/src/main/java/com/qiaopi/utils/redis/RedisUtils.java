package com.qiaopi.utils.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 500;

    /**
     * 从 Redis 获取值，支持重试机制
     *
     * @param key Redis 键
     * @return Redis 值
     */
    public String getWithRetry(String key) {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                return stringRedisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    log.error("从 Redis 获取键 {} 失败，达到最大重试次数", key, e);
                    throw e; // 抛出异常，不再重试
                }
                log.warn("从 Redis 获取键 {} 失败，尝试第 {} 次重试", key, retryCount);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试等待被中断", ie);
                }
            }
        }
        return null; // 如果所有重试都失败，返回 null
    }

    /**
     * 设置 Redis 键值对，支持重试机制
     *
     * @param key   Redis 键
     * @param value Redis 值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public void setWithRetry(String key, String value, long timeout, TimeUnit unit) {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    log.error("设置 Redis 键 {} 失败，达到最大重试次数", key, e);
                    throw e; // 抛出异常，不再重试
                }
                log.warn("设置 Redis 键 {} 失败，尝试第 {} 次重试", key, retryCount);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试等待被中断", ie);
                }
            }
        }
    }

    /**
     * 设置 Redis 键值对，支持重试机制，不带超时时间
     *
     * @param key   Redis 键
     * @param value Redis 值
     */
    public void setWithRetry(String key, String value) {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                stringRedisTemplate.opsForValue().set(key, value);
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    log.error("设置 Redis 键 {} 失败，达到最大重试次数", key, e);
                    throw e; // 抛出异常，不再重试
                }
                log.warn("设置 Redis 键 {} 失败，尝试第 {} 次重试", key, retryCount);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试等待被中断", ie);
                }
            }
        }
    }
}

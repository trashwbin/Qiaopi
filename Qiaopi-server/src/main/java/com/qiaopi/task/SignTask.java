package com.qiaopi.task;


import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.qiaopi.constant.CacheConstant.*;

/**
 * 自定义定时任务，实现信件任务定时处理
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SignTask {

    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteTodaySignCache(){
        log.info("删除今日签到缓存");
        Set<String> keys = stringRedisTemplate.keys(SIGN_TODAY_ALL_KEY);
        if (!Collections.isEmpty(keys)) {
            assert keys != null;
            stringRedisTemplate.delete(keys);
        }
    }

}

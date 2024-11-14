package com.qiaopi.task;


import cn.hutool.json.JSONUtil;
import com.qiaopi.entity.UserSignAward;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteTodaySignCache(){
        log.info("删除昨日签到缓存");
        LocalDateTime now = LocalDateTime.now();
        // 删除昨天的缓存
        String key = SIGN_TODAY_KEY +now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+":*";
        Set<String> keys = stringRedisTemplate.keys(key);
        if (!Collections.isEmpty(keys)) {
            assert keys != null;
            stringRedisTemplate.delete(keys);
        }
    }
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteTodayGameCache(){
        log.info("删除昨日翻翻乐缓存");
        // 删除昨天的缓存
        String key = GAME_FFL_USER_KEY + "*";
        Set<String> keys = stringRedisTemplate.keys(key);
        if (!Collections.isEmpty(keys)) {
            assert keys != null;
            stringRedisTemplate.delete(keys);
        }
    }
    @Scheduled(cron = "0 0 0 * * 1 ")
    public void deleteSignAwardCache(){
        log.info("刷新签到奖励");
        List<UserSignAward> userSignAwardList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            UserSignAward userSignAward = new UserSignAward();
            userSignAward.setId((long) i+1);
            userSignAward.setAwardType(1);
            userSignAward.setAwardDesc("猪仔钱");
            userSignAward.setAwardName("猪仔钱");
            userSignAward.setAwardNum((i+1)*10);
            userSignAward.setPreviewLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/card/0.webp");
            userSignAward.setSignDays(i + 1);
            userSignAwardList.add(userSignAward);
        }
        LocalDateTime now = LocalDateTime.now();
        String prefix = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        stringRedisTemplate.opsForValue().set(SIGN_AWARD_KEY + prefix, JSONUtil.toJsonStr(userSignAwardList));
        stringRedisTemplate.opsForValue().set(SIGN_CURRENT_KEY, prefix);
    }
}

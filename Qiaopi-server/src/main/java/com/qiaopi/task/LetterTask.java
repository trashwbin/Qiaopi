package com.qiaopi.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.constant.LetterConstants;
import com.qiaopi.entity.Letter;
import com.qiaopi.mapper.LetterMapper;
import com.qiaopi.service.LetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.qiaopi.constant.CacheConstant.*;

/**
 * 自定义定时任务，实现信件任务定时处理
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LetterTask {

    private final LetterMapper letterMapper;

    private final LetterService letterService;
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 处理信件任务
     */
     @Scheduled(cron = "0 * * * * ?")
    public void processLetterTask(){
         List<Letter> letters = letterMapper.selectList(new LambdaQueryWrapper<Letter>()
                 .le(Letter::getDeliveryTime, LocalDateTime.now())
                 .eq(Letter::getStatus, LetterConstants.TRANSIT));
         if (!letters.isEmpty()) {
             letterService.sendLetterToEmail(letters);
         }
         log.info("处理信件任务：{},送信数:{}", System.currentTimeMillis(),letters.size());
    }
}

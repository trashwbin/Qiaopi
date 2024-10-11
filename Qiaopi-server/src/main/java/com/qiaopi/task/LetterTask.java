package com.qiaopi.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.entity.Letter;
import com.qiaopi.mapper.LetterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义定时任务，实现信件任务定时处理
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LetterTask {

    private final LetterMapper letterMapper;

    /**
     * 处理信件任务
     */
     @Scheduled(cron = "0 * * * * ?")
    public void processLetterTask(){

        log.info("处理信件任务：{},送信数:{}", System.currentTimeMillis(),1);

         List<Letter> letters = letterMapper.selectList(new LambdaQueryWrapper<Letter>()
                 .le(Letter::getExpectedDeliveryTime, System.currentTimeMillis())
                 .eq(Letter::getStatus, 2));
        

     }


}

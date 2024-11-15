package com.qiaopi.task;


import cn.hutool.json.JSONUtil;
import com.qiaopi.entity.FunctionCard;
import com.qiaopi.entity.UserSignAward;
import com.qiaopi.mapper.CardMapper;
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
    private final CardMapper cardMapper;

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

    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteTodayTaskCache(){
        log.info("删除昨日任务缓存");
        LocalDateTime now = LocalDateTime.now();
        // 删除昨天的缓存
        String key = "task"+":*:"+ now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        //String key = "task"+":*:"+ "2024-11-15";
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
            userSignAward.setAwardId(0L);
//            userSignAward.setPreviewLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/card/0.webp");
            userSignAward.setPreviewLink("data:image/webp;base64,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\n");
            //data:image/webp;base64,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
            userSignAward.setSignDays(i + 1);
            userSignAwardList.add(userSignAward);
        }
        FunctionCard functionCard = cardMapper.selectById(2L);
        userSignAwardList.set(1,UserSignAward.builder().id(2L).signDays(2).previewLink(functionCard.getCardPreviewLink()).awardType(2).awardName(functionCard.getCardName()).awardDesc(functionCard.getCardDesc()).awardNum(1).awardId(functionCard.getId()).build());
        functionCard = cardMapper.selectById(8L);
        userSignAwardList.set(3,UserSignAward.builder().id(4L).signDays(4).previewLink(functionCard.getCardPreviewLink()).awardType(2).awardName(functionCard.getCardName()).awardDesc(functionCard.getCardDesc()).awardNum(1).awardId(functionCard.getId()).build());
        functionCard = cardMapper.selectById(0);
        userSignAwardList.set(5,UserSignAward.builder().id(6L).signDays(6).previewLink(functionCard.getCardPreviewLink()).awardType(2).awardName(functionCard.getCardName()).awardDesc(functionCard.getCardDesc()).awardNum(1).awardId(functionCard.getId()).build());

        userSignAwardList.set(6,UserSignAward.builder().id(7L).signDays(7).previewLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/font/06.png").awardType(3).awardName("随机字体").awardDesc("随机获得您未拥有的字体字体").awardNum(1).awardId(0L).build());

        LocalDateTime now = LocalDateTime.now();
        String prefix = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        stringRedisTemplate.opsForValue().set(SIGN_AWARD_KEY + prefix, JSONUtil.toJsonStr(userSignAwardList));
        stringRedisTemplate.opsForValue().set(SIGN_CURRENT_KEY, prefix);
    }
}

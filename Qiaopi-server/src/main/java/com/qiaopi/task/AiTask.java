package com.qiaopi.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.qiaopi.constant.AiConstant;
import com.qiaopi.handler.Ai.ChatSocketHandler;
import com.qiaopi.handler.Ai.pojo.AiInteractData;
import com.qiaopi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiTask {
    private final StringRedisTemplate stringRedisTemplate;
    private final ChatService chatService;
    private final LinkedList<Long> recentlySentUsers = new LinkedList<>();

    @Scheduled(cron = "0 * * * * ?")
    public void sendInteractMessage() {
        List<AiInteractData> aiInteractData = JSON.parseArray(stringRedisTemplate.opsForValue().get(AiConstant.INTERACTIVE_LIST), AiInteractData.class);
        if (CollUtil.isEmpty(aiInteractData)) {
            return;
        }
        // After
        AiInteractData data = aiInteractData.get(ThreadLocalRandom.current().nextInt(aiInteractData.size()));
        ChatSocketHandler.getSessions().stream()
                .filter(WebSocketSession::isOpen)
                .limit(2)
                .forEach(session -> {
                    Long userId = (Long) session.getAttributes().get("userId");
                    if (userId != null && !recentlySentUsers.contains(userId)) {
                        chatService.sendInteractiveMessage(userId, data.getMessage(), data.getRouter());
                        recentlySentUsers.addLast(userId);
                        log.info("send interact message {} to user: {}", data, userId);
                    }
                });
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void clearRecentlySentUsers() {
        for (int i = 0; i < 2; i++) {
            if (!recentlySentUsers.isEmpty()) {
                recentlySentUsers.removeFirst();
            }
        }
    }
}

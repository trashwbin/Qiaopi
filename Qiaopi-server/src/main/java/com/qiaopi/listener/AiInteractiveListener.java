package com.qiaopi.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.qiaopi.constant.AiConstant;
import com.qiaopi.handler.Ai.pojo.AiInteractData;
import com.qiaopi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import static com.qiaopi.constant.MqConstant.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiInteractiveListener {

    private final ChatService chatService;
    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = QUEUE_AI_INTERACT, durable = "true"),
            exchange = @Exchange(name = EXCHANGE_AI_DIRECT, type = ExchangeTypes.DIRECT),
            key = ROUTING_KEY_INTERACT
    ))
    public void listenAiInteractiveMessage(ConcurrentHashMap<String, Object> map) {
        Long userId = ((Number) map.get("userId")).longValue();
        String message = (String) map.get("message");
        Object data = map.get("data");
        chatService.sendInteractiveMessage(userId, message, data);
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = QUEUE_AI_WRITE_LETTER, durable = "true" ),
            exchange = @Exchange(name = EXCHANGE_AI_DIRECT, type = ExchangeTypes.DIRECT),
            key = ROUTING_KEY_WRITE_LETTER
    ))
    public void listenWriteLetterMessage(Long userId) {
        List<AiInteractData> aiInteractData = JSON.parseArray(stringRedisTemplate.opsForValue().get(AiConstant.INTERACTIVE_WRITE_LETTER), AiInteractData.class);
        if (CollUtil.isEmpty(aiInteractData)) {
            return;
        }
        AiInteractData data = aiInteractData.get(ThreadLocalRandom.current().nextInt(aiInteractData.size()));
        chatService.sendInteractiveMessage(userId, data.getMessage(), data.getRouter());
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = QUEUE_AI_MARKETING, durable = "true" ),
            exchange = @Exchange(name = EXCHANGE_AI_DIRECT, type = ExchangeTypes.DIRECT),
            key = ROUTING_KEY_MARKETING
    ))
    public void listenMarketingMessage(Long userId) {
        List<AiInteractData> aiInteractData = JSON.parseArray(stringRedisTemplate.opsForValue().get(AiConstant.INTERACTIVE_MARKETING), AiInteractData.class);
        if (CollUtil.isEmpty(aiInteractData)) {
            return;
        }
        AiInteractData data = aiInteractData.get(ThreadLocalRandom.current().nextInt(aiInteractData.size()));
        chatService.sendInteractiveMessage(userId, data.getMessage(), data.getRouter());
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = QUEUE_AI_SIGN_AWARD, durable = "true" ),
            exchange = @Exchange(name = EXCHANGE_AI_DIRECT, type = ExchangeTypes.DIRECT),
            key = ROUTING_KEY_SIGN_AWARD
    ))
    public void listenSignMessage(ConcurrentHashMap<String, Object> map) {
        Long userId = ((Number) map.get("userId")).longValue();
        String message = (String) map.get("message");
        chatService.sendInteractiveMessage(userId, message, "");
    }
}
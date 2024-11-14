package com.qiaopi.controller.other;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.qiaopi.constant.AiConstant;
import com.qiaopi.handler.Ai.pojo.AiData;
import com.qiaopi.handler.Ai.pojo.AiInteractData;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.ChatService;
import com.qiaopi.utils.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.qiaopi.result.AjaxResult.error;
import static com.qiaopi.result.AjaxResult.success;
import static com.qiaopi.utils.MessageUtils.message;

/**
 * 通用接口
 */
@RestController
@RequestMapping()
@Tag(name = "测试")
@Slf4j
@RequiredArgsConstructor
public class TestController {
    private final StringRedisTemplate stringRedisTemplate;
    private final ChatService chatService;
    private final RabbitTemplate rabbitTemplate;
    @PostMapping("/testSendInteractMessage")
    public void sendInteractMessage(Long userId) {
        List<AiInteractData> aiInteractData = JSON.parseArray(stringRedisTemplate.opsForValue().get(AiConstant.INTERACTIVE_LIST), AiInteractData.class);
        assert aiInteractData != null;
        AiInteractData data = aiInteractData.get(RandomUtil.randomInt(aiInteractData.size()));
//        chatService.sendInteractiveMessage(userId,data.getMessage(),data.getRouter());
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        map.put("userId",userId);
        map.put("message",data.getMessage());
        map.put("data",data.getRouter());
        rabbitTemplate.convertAndSend("ai.direct","interact",map);
    }
}
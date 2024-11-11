package com.qiaopi.handler.Ai;

import cn.hutool.cron.timingwheel.SystemTimer;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiaopi.constant.AiConstant;
import com.qiaopi.dto.ChatDTO;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.ChatService;
import com.qiaopi.service.G2dService;
import com.qiaopi.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final StringRedisTemplate stringRedisTemplate;
  // 用于保存所有连接的 WebSocket 会话
    private static final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // 用于保存用户与 WebSocket 会话的映射
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // 当有客户端连接时调用
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 WebSocket 会话中获取用户 ID

        Long userId = (Long) session.getAttributes().get("userId");
        log.debug("用户 ID: {} 连接中", userId);
        // 如果用户 ID 不为 null，将用户会话存储在 userSessions 中
        if (userId != null) {
            userSessions.put(userId, session);
            sessions.add(session); // 添加到所有会话集合中
            log.debug("用户 ID: {} 的会话已存储", userId);
            ChatSocketHandler.sendMessageToUser(userId, JSON.toJSONString(AjaxResult.success(MessageUtils.message("chat.connect.success"))));
        } else {
            log.error("无法获取用户 ID，无法存储会话。");
            session.close(); // 关闭会话
        }
    }

    // 当有消息从客户端发送过来时调用
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        log.info("收用户1到消息: {}", message.getPayload());
        // 从 session 的 attributes 中获取用户 ID
        Long currentUserId = (Long) session.getAttributes().get("userId");

        // 获取传来的 message 信息
        String payload = message.getPayload();
        ChatDTO chatDTO = JSONUtil.toBean(payload, ChatDTO.class);
        chatDTO.setUserId(currentUserId);
        chatDTO.setMessage(chatDTO.getMessage().trim());
        log.info("收到用户 {} 的消息: {}", currentUserId, chatDTO);

        switch (chatDTO.getMessage()) {
            case AiConstant.ORDER_CLEAR, AiConstant.ORDER_NEW:
                chatService.storeChat(currentUserId);
                break;
            default:
                chatService.chat(chatDTO);
                break;
        }
    }

    // 当连接关闭时调用
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("用户 ID: {} 断开连接",userId );
        // 将用户对话存储到 chat:user:userId:timestamp
        chatService.storeChat(userId);
        // 从 userSessions 中移除用户会话
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    // 向特定用户发送消息
    public static void sendMessageToUser(Long userId, String responseEntity) { // 使用 Long 类型
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(responseEntity));
            } catch (Exception e) {
                log.error("发送消息失败: ", e);
            }
        } else {
            log.info("用户会话已关闭或不存在: {}", userId);
        }
    }

    // 广播消息给所有连接的客户端
    public void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
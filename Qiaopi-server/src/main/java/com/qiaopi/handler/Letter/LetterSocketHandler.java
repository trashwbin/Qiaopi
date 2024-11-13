package com.qiaopi.handler.Letter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.service.LetterService;
import com.qiaopi.service.G2dService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LetterSocketHandler extends TextWebSocketHandler {

    @Autowired
    private G2dService g2dService;

  // 用于保存所有连接的 WebSocket 会话
    private static final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // 用于保存用户与 WebSocket 会话的映射
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // 当有客户端连接时调用
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 WebSocket 会话中获取用户 ID
        Long userId = (Long) session.getAttributes().get("userId");
        System.out.println("用户 ID: " + userId);

        // 如果用户 ID 不为 null，将用户会话存储在 userSessions 中
        if (userId != null) {
            userSessions.put(userId, session);
            sessions.add(session); // 添加到所有会话集合中
            session.sendMessage(new TextMessage("success")); // 发送消息给客户端
        } else {
            log.warn("无法获取用户 ID，无法存储会话。");
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

        // 创建 ObjectMapper 实例
        // 将 JSON 数据转换为 LetterGenDTO 对象
        ObjectMapper objectMapper = new ObjectMapper();
        LetterGenDTO letterGenDTO = objectMapper.readValue(payload, LetterGenDTO.class);

        // 调用 Service 方法
        String base64Result = g2dService.generateImage(letterGenDTO, currentUserId);
        //String base64Result = letterService.generateImage(letterGenDTO, currentUserId);

        // 将生成的 URL 发送给特定用户
        if (currentUserId != null) {
            sendMessageToUser(currentUserId, base64Result);
        } else {
            log.error("无法获取用户 ID，无法发送消息。");
        }
    }

    // 当连接关闭时调用
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);

        // 从 userSessions 中移除用户会话
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    // 向特定用户发送消息
    public void sendMessageToUser(Long userId, String responseEntity) { // 使用 Long 类型
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
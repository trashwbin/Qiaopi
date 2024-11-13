package com.qiaopi.handler.Ai;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.qiaopi.constant.AiConstant;
import com.qiaopi.dto.ChatDTO;
import com.qiaopi.handler.Ai.pojo.AiData;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.ChatService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.qiaopi.constant.AiConstant.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final StringRedisTemplate stringRedisTemplate;

    // 用于保存所有连接的 WebSocket 会话
    @Getter
    private static final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // 用于保存用户与 WebSocket 会话的映射
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // 用于保存离线用户及其最后在线时间
    private static final Map<Long, LocalDateTime> offlineUsers = new ConcurrentHashMap<>();

    // 定时任务执行器
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    // 当有客户端连接时调用
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        log.debug("用户 ID: {} 连接中", userId);

        if (userId != null) {
            userSessions.put(userId, session);
            sessions.add(session); // 添加到所有会话集合中
            log.debug("用户 ID: {} 的会话已存储", userId);
        if (offlineUsers.containsKey(userId)) {
            chatService.getChattingHistory(userId);
        } else {
            sendMessageToUser(userId, JSON.toJSONString(AjaxResult.success(MessageUtils.message("chat.connect.success"),new AiData(TYPE_SYSTEM,CODE_CONNECT, null))));
        }
            // 用户重新连接，移除离线状态
            offlineUsers.remove(userId);
        } else {
            log.error("无法获取用户 ID，无法存储会话。");
            session.close(); // 关闭会话
        }
    }

    // 当有消息从客户端发送过来时调用
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long currentUserId = (Long) session.getAttributes().get("userId");

        if (currentUserId == null) {
            log.error("无法获取用户 ID，无法处理消息。");
            return;
        }

        String payload = message.getPayload();
        ChatDTO chatDTO = JSONUtil.toBean(payload, ChatDTO.class);
        chatDTO.setUserId(currentUserId);
        if (StringUtils.isEmpty(chatDTO.getMessage())) {
            log.error("消息内容为空，无法处理。");
            return;
        }
        chatDTO.setMessage(chatDTO.getMessage().trim());
        log.debug("收到用户 {} 的消息: {}", currentUserId, chatDTO);

        switch (chatDTO.getMessage()) {
            case AiConstant.ORDER_CLEAR, AiConstant.ORDER_NEW:
                chatService.storeChat(currentUserId);
                break;
            case AiConstant.ORDER_HISTORY:
                chatService.getChatHistory(currentUserId);
                break;
            case AiConstant.ORDER_HELP:
                chatService.help(currentUserId);
                break;
            case AiConstant.ORDER_RETRY:
                chatService.retry(currentUserId);
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
        log.debug("用户 ID: {} 断开连接", userId);

        if (userId != null) {
            // 标记用户为离线状态
            offlineUsers.put(userId, LocalDateTime.now());
            log.debug("用户 ID: {} 已标记为离线", userId);
        }
    }

    // 向特定用户发送消息
    public static void sendMessageToUser(Long userId, String responseEntity) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(responseEntity));
            } catch (Exception e) {
                log.error("发送消息失败: ", e);
            }
        }
        //else {
        //    log.warn("用户会话已关闭或不存在: {}", userId);
        //}
    }

    // 广播消息给所有连接的客户端
    public void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("广播消息失败: ", e);
            }
        }
    }

    // 初始化定时任务，检查离线用户
    @PostConstruct
    public void initOfflineCheck() {
        executorService.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            for (Map.Entry<Long, LocalDateTime> entry : offlineUsers.entrySet()) {
                Long userId = entry.getKey();
                LocalDateTime lastOnlineTime = entry.getValue();
                long minutesSinceLastOnline = java.time.Duration.between(lastOnlineTime, now).toMinutes();

                if (minutesSinceLastOnline > 5) { // 假设5分钟为离线超时时间
                    log.debug("用户 ID: {} 超过5分钟未重新连接，移除会话", userId);
                    chatService.storeChat(userId);
                    userSessions.remove(userId);
                    offlineUsers.remove(userId);
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }
}

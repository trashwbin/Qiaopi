package com.qiaopi.service;

import com.qiaopi.dto.ChatDTO;

public interface ChatService {
    boolean testSseInvoke(String message);


    void chat(ChatDTO chatDTO);

    void storeChat(Long userId);

    void getChatHistory(Long userId);
    void getChattingHistory(Long userId);

    void help(Long currentUserId);

    void retry(Long currentUserId);

    void sendInteractiveMessage(Long userId, String message, Object data);
}

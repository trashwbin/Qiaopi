package com.qiaopi.service;

import com.qiaopi.dto.ChatDTO;

public interface ChatService {
    boolean testSseInvoke(String message);


    void chat(ChatDTO chatDTO);

    void storeChat(Long userId);
}

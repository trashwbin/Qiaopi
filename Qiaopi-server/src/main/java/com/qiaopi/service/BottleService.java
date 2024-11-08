package com.qiaopi.service;

import com.qiaopi.dto.BottleGenDTO;
import com.qiaopi.dto.FriendSendDTO;
import com.qiaopi.entity.Address;
import com.qiaopi.vo.BottleVo;

import java.util.List;

public interface BottleService {


    /**
     * 漂流瓶生成
     * @param bottleGenDTO
     */
    String generateDriftBottle(BottleGenDTO bottleGenDTO);

    String  getBottle();

    void sendFriendRequest(FriendSendDTO friendSendDTO);


    void ThrowBack();
}

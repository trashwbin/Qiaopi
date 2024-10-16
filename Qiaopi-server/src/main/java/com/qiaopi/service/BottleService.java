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
    String GenerateDriftBottle(BottleGenDTO bottleGenDTO);

    String  showBottle();

    void getBottle(FriendSendDTO friendSendDTO);

    /**
     * 扔回漂流瓶
     */
    void ThrowBack();
}

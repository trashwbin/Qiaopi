package com.qiaopi.service;

import com.qiaopi.dto.BeFriendDTO;
import com.qiaopi.entity.FriendRequest;
import com.qiaopi.vo.BottleVo;

import java.util.List;

public interface FriendService {



    /**
     * 请求成为好友
     */
    String sendFriendRequest(Long id);

    /**
     * 处理好友申请
     * @return
     */
    List<FriendRequest> ProcessingFriendRequests();


    /**
     * 成为好友
     */
    String BecomeFriend(BeFriendDTO beFriendDTO);
}

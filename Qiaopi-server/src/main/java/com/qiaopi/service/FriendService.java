package com.qiaopi.service;

import com.qiaopi.dto.BeFriendDTO;
import com.qiaopi.vo.FriendRequestVO;

import java.util.List;

public interface FriendService {



 /*   *//**
     * 请求成为好友
     */
//    void sendFriendRequest();
    /**
     * 处理好友申请
     *
     * @return
     */
    List<FriendRequestVO> ProcessingFriendRequests();


    /**
     * 成为好友
     */
    String becomeFriend(BeFriendDTO beFriendDTO);


}

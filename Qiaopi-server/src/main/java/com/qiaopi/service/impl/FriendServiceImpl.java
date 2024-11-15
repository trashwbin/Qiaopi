package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiaopi.constant.FriendConstants;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.BeFriendDTO;
import com.qiaopi.entity.*;
import com.qiaopi.exception.bottle.BottleException;
import com.qiaopi.exception.friend.FriendException;
import com.qiaopi.mapper.*;
import com.qiaopi.service.BottleService;
import com.qiaopi.service.FriendService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.FriendRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.qiaopi.constant.CacheConstant.CACHE_USER_FRIENDS_KEY;

@Service
@Slf4j
@RequiredArgsConstructor//自动注入
public class FriendServiceImpl implements FriendService {


    @Autowired
    private FriendRequestMapper friendRequestMapper;
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private BottleMapper bottleMapper;


    @Autowired
    private final BottleService bottleService;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LetterMapper letterMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

/*

    @Override
    public String sendFriendRequest(Long id) {
        Long currentUserId  = null;
        try {
            //获取到当前请求的用户id
            currentUserId = UserContext.getUserId();
        } catch (Exception e) {
            throw new BottleException(MessageUtils.message("bottle.getCurrentId.failed"));
        }

        // 重新从数据库中获取最新的 Bottle，确保数据一致性和安全性
        Bottle latestBottle = bottleMapper.selectById(id);


        // 校验：当前用户是否有权操作此漂流瓶
        if (!canCurrentUserOperateBottle(currentUserId, latestBottle)) {
            throw new FriendException(MessageUtils.message("friend.no.bottle.permission"));
        }

        try {
            // 从最新的 Bottle 对象中获取 userId，并发送好友请求
            Long targetUserId = latestBottle.getUserId();

            // 插入好友申请
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setSenderId(currentUserId);
            friendRequest.setReceiverId(targetUserId);
            friendRequest.setStatus(0); // 0表示待处理
            friendRequestMapper.insert(friendRequest);
        } catch (Exception e) {
            throw new FriendException(MessageUtils.message("friend.create.Request.failed"));
        }

        //String replySuccess = "好友申请已发送";
        String replySuccess = MessageUtils.message("friend.request.sended.success");
        return replySuccess;

    }

*/



    /**
     * 校验当前用户是否有权操作指定的 Bottle
     * @param currentUserId 当前操作的用户ID
     * @param latestBottle 当前的 Bottle 对象
     * @return 是否有权限
     */
    private boolean canCurrentUserOperateBottle(Long currentUserId, Bottle latestBottle) {
        // 假设只有当漂流瓶已经被捡走且当前用户是更新者时，才允许操作
        return latestBottle.isPicked() == true && latestBottle.getUpdateUser().equals(currentUserId);
    }


/*    @Override
    public void sendFriendRequest() {
        // 插入好友申请
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(currentUserId);
        friendRequest.setReceiverId(targetUserId);
        friendRequest.setStatus(0); // 0表示待处理
        friendRequest.setGiveAddress(friendSendDTO.getGiveAddresss());
        friendRequest.setContent(friendSendDTO.getContext());
        friendRequest.setBottleId(bottle.getId());
        friendRequestMapper.insert(friendRequest);
    }*/

    @Override
    public List<FriendRequestVO> ProcessingFriendRequests() {
        Long receiverId = null;
        try {
            //获取当前线程的用户id 此当前用户的id也为被请求人id
            receiverId = UserContext.getUserId();
        } catch (Exception e) {
            throw new BottleException(MessageUtils.message("bottle.getCurrentId.failed"));
        }

        QueryWrapper<FriendRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", receiverId).eq("status", FriendConstants.PENDING); // 查询待处理的请求

        List<FriendRequestVO> friendRequestVOS = BeanUtil.copyToList(friendRequestMapper.selectList(queryWrapper), FriendRequestVO.class);
        friendRequestVOS.stream().collect(Collectors.groupingBy(FriendRequestVO::getSenderId));
        if (friendRequestVOS.isEmpty()) {
            return Collections.emptyList();
        }
        userMapper.selectBatchIds(friendRequestVOS.stream().map(FriendRequestVO::getSenderId).collect(Collectors.toList()))
                .forEach(user -> {
                    friendRequestVOS.stream().filter(friendRequestVO -> friendRequestVO.getSenderId().equals(user.getId()))
                            .forEach(friendRequestVO -> {
                                friendRequestVO.setSenderName(user.getNickname());
                                friendRequestVO.setSenderAvatar(user.getAvatar());
                            });
                });

        return friendRequestVOS;
    }

    @Override
    public String becomeFriend( BeFriendDTO beFriendDTO) {
        Long requestId = beFriendDTO.getRequestId();
        Long longIsAccepted = beFriendDTO.getIsAccepted();
        boolean isAccepted;

        if (longIsAccepted == 1) {
            isAccepted = true;
        } else {
            isAccepted = false;
        }
        Long currentUserId = UserContext.getUserId();

        FriendRequest friendRequest = friendRequestMapper.selectById(requestId);

        if (friendRequest == null || friendRequest.getStatus() != 0) {
            //若好友申请不存在或者已处理
            throw new FriendException(MessageUtils.message("friend.friendRequest.empty"));
        }

        // 检查当前用户是否是接收者
        if (!friendRequest.getReceiverId().equals(currentUserId)) {
            //您没有权限处理此好友申请
            throw new FriendException(MessageUtils.message("friend.friendRequest.no.permission"));
        }

        Bottle bottle = new Bottle();
        Letter letter = new Letter();
        Address mineToFriendAddresses = null;//请求人的地址
        //判断当前的FriendRequest中含有的是bottleId还是letterId
        if (friendRequest.getBottleId() != null) {
            bottle = bottleMapper.selectById(friendRequest.getBottleId());
            mineToFriendAddresses = bottle.getSenderAddress();
        }else if (friendRequest.getLetterId() != null) {
            letter = letterMapper.selectById(friendRequest.getLetterId());
            mineToFriendAddresses = letter.getSenderAddress();
        }
        if (bottle == null || letter == null) {
            throw new FriendException(MessageUtils.message("friend.bottle.not.exists"));
        }

        if (isAccepted) {
            // 更新好友申请状态为已接受
            friendRequest.setStatus(FriendConstants.AGREE); // 1表示已接受
            friendRequest.setUpdateTime(LocalDateTime.now());

            try {
                // 添加双方的好友关系
                Address friendToMeAddress = friendRequest.getGiveAddress();

                addFriend(friendRequest.getReceiverId(), friendRequest.getSenderId(), mineToFriendAddresses,friendToMeAddress);
            } catch (Exception e) {
                throw new FriendException(MessageUtils.message("friend.add.failed"));
            }

            friendRequestMapper.updateById(friendRequest);

            return MessageUtils.message("friend.application.accepted");
        } else {
            // 更新好友申请状态为已拒绝
            friendRequest.setStatus(FriendConstants.REFUSE); // 2表示已拒绝
            friendRequest.setUpdateTime(LocalDateTime.now());
            friendRequestMapper.updateById(friendRequest);
            return MessageUtils.message("friend.application.refused");
        }

    }

    // 添加好友关系
    private void addFriend(Long userId, Long friendId, Address mineToFriendAddresses, Address friendToMeAddress) {
        //先设置用户的好友信息
        Friend myNewFriend = new Friend();


        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", friendId);
        User friend = userMapper.selectOne(queryWrapper);//获取到好友的信息，并将其设置为用户的好友信息

        myNewFriend.setUserId(friendId);
        myNewFriend.setName(friend.getNickname());
        myNewFriend.setSex(friend.getSex());
        myNewFriend.setEmail(friend.getEmail());
        myNewFriend.setAddresses(Collections.singletonList(friendToMeAddress));
        myNewFriend.setOwningId(userId);
        myNewFriend.setAvatar(friend.getAvatar());
        myNewFriend.setRemark(friend.getNickname());
        friendMapper.insert(myNewFriend);


        // 互相添加好友
        //对方添加自己为好友
        Friend BecomeFriendOfTheOtherParty = new Friend();

        // 创建 QueryWrapper，用来构造查询条件
        QueryWrapper<User> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("id", userId);
        User myInfoOfUser = userMapper.selectOne(queryWrapper2);

        BecomeFriendOfTheOtherParty.setUserId(userId);
        BecomeFriendOfTheOtherParty.setName(myInfoOfUser.getNickname());
        BecomeFriendOfTheOtherParty.setSex(myInfoOfUser.getSex());
        BecomeFriendOfTheOtherParty.setEmail(myInfoOfUser.getEmail());
        BecomeFriendOfTheOtherParty.setAddresses(Collections.singletonList(mineToFriendAddresses));
        BecomeFriendOfTheOtherParty.setOwningId(friendId);
        BecomeFriendOfTheOtherParty.setAvatar(friend.getAvatar());
        BecomeFriendOfTheOtherParty.setRemark(friend.getNickname());

        friendMapper.insert(BecomeFriendOfTheOtherParty);

        // 删除缓存
        stringRedisTemplate.delete(CACHE_USER_FRIENDS_KEY + userId);
        stringRedisTemplate.delete(CACHE_USER_FRIENDS_KEY + friendId);
    }

}





package com.qiaopi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.BeFriendDTO;
import com.qiaopi.entity.Bottle;
import com.qiaopi.entity.Friend;
import com.qiaopi.entity.FriendRequest;
import com.qiaopi.entity.User;
import com.qiaopi.mapper.BottleMapper;
import com.qiaopi.mapper.FriendMapper;
import com.qiaopi.mapper.FriendRequestMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.BottleService;
import com.qiaopi.service.FriendService;
import com.qiaopi.vo.BottleVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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


    @Override
    public String sendFriendRequest(BottleVo bottleVo) {
        //获取到当前请求的用户id
        Long currentUserId  = UserContext.getUserId();

        // 重新从数据库中获取最新的 Bottle，确保数据一致性和安全性
        Bottle latestBottle = bottleMapper.selectById(bottleVo.getId());


        // 校验：当前用户是否有权操作此漂流瓶
        if (!canCurrentUserOperateBottle(currentUserId, latestBottle)) {
            String replyEnable = "你无权操作该漂流瓶";
            log.info("用户无权操作该漂流瓶");
            return replyEnable;
        }

        // 从最新的 Bottle 对象中获取 userId，并发送好友请求
        Long targetUserId = latestBottle.getUserId();

        // 插入好友申请
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(currentUserId);
        friendRequest.setReceiverId(targetUserId);
        friendRequest.setStatus(0); // 0表示待处理
        friendRequestMapper.insert(friendRequest);

        String replySuccess = "好友申请已发送";
        return "好友申请已发送";

    }




    /**
     * 校验当前用户是否有权操作指定的 Bottle
     * @param currentUserId 当前操作的用户ID
     * @param latestBottle 当前的 Bottle 对象
     * @return 是否有权限
     */
    private boolean canCurrentUserOperateBottle(Long currentUserId, Bottle latestBottle) {
        // 假设只有当漂流瓶已经被捡走且当前用户是更新者时，才允许操作
        return latestBottle.getIsPicked() == 1 && latestBottle.getUpdateUser().equals(currentUserId);
    }


    @Override
    public List<FriendRequest> ProcessingFriendRequests() {
        //获取当前线程的用户id 此当前用户的id也为被请求人id
        Long receiverId = UserContext.getUserId();

        QueryWrapper<FriendRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", receiverId).eq("status", 0); // 查询待处理的请求

        return friendRequestMapper.selectList(queryWrapper);
    }

    @Override
    public String BecomeFriend( BeFriendDTO beFriendDTO) {
        Long requestId = beFriendDTO.getRequestId();
        Long longIsAccepted = beFriendDTO.getIsAccepted();
        Boolean isAccepted;
        if (longIsAccepted == 1) {
            isAccepted = true;
        } else {
            isAccepted = false;
        }

        //获取当前线程用户的id
        Long currentUserId = UserContext.getUserId();

        //根据用户id找到对应的请求
        FriendRequest friendRequest = friendRequestMapper.selectById(requestId);



        if (friendRequest == null || friendRequest.getStatus() != 0) {
            //若好友申请不存在或者已处理
            String replyempty = "暂无好友申请";
            return replyempty;
        }

        // 检查当前用户是否是接收者
        if (!friendRequest.getReceiverId().equals(currentUserId)) {
            String replyUnable = "您没有权限处理此好友申请";
            return replyUnable;
        }


        if (isAccepted) {
            // 更新好友申请状态为已接受
            friendRequest.setStatus(1); // 1表示已接受
            friendRequest.setUpdateTime(LocalDateTime.now());

            // 添加双方的好友关系
            addFriend(friendRequest.getReceiverId(), friendRequest.getSenderId());

            friendRequestMapper.updateById(friendRequest);

            String replyAccept = "已接受好友申请";
            return replyAccept;
        } else {
            // 更新好友申请状态为已拒绝
            friendRequest.setStatus(2); // 2表示已拒绝
            friendRequest.setUpdateTime(LocalDateTime.now());
            friendRequestMapper.updateById(friendRequest);
            String replyRefuse = "好友申请已拒绝";
            return replyRefuse;
        }

    }

    // 添加好友关系
    private void addFriend(Long userId, Long friendId) {
        //先设置用户的好友信息
        Friend myNewFriend = new Friend();


        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", friendId);
        User friend = userMapper.selectOne(queryWrapper);//获取到好友的信息，并将其设置为用户的好友信息

        myNewFriend.setUserId(friendId);
        myNewFriend.setName(friend.getNickname());
        myNewFriend.setSex(friend.getSex());
        myNewFriend.setEmail(friend.getEmail());
        myNewFriend.setAddresses(friend.getAddresses());
        myNewFriend.setOwningId(userId);
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
        BecomeFriendOfTheOtherParty.setAddresses(myInfoOfUser.getAddresses());
        BecomeFriendOfTheOtherParty.setOwningId(friendId);

        friendMapper.insert(BecomeFriendOfTheOtherParty);
    }

}





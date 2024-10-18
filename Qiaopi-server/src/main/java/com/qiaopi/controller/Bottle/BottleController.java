package com.qiaopi.controller.Bottle;


import com.qiaopi.dto.BeFriendDTO;
import com.qiaopi.dto.BottleGenDTO;
import com.qiaopi.dto.FriendSendDTO;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.Address;
import com.qiaopi.entity.FriendRequest;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.BottleService;
import com.qiaopi.service.FriendService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.BottleVo;
import com.qiaopi.vo.FriendRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bottle")
@Slf4j
@Tag(name = "漂流瓶相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BottleController {

    @Autowired
    private BottleService bottleService;

    @Autowired
    private FriendService friendService;


    @PostMapping("/generateDriftBottle")
    @Operation(summary = "写漂流瓶")
    public AjaxResult GenerateDriftBottle(@RequestBody BottleGenDTO bottleGenDTO) {

        log.info("生成漂流瓶：{}", bottleGenDTO);
        String url = bottleService.GenerateDriftBottle(bottleGenDTO);
        return AjaxResult.success(MessageUtils.message("bottle.generateImage.success"),url);
    }


    @GetMapping("/showDriftBottle")
    @Operation(summary = "拿一个漂流瓶")
    public AjaxResult showtBottleById() {
        log.info("展示漂流瓶");
        String url = bottleService.showBottle();
        return AjaxResult.success(MessageUtils.message("bottle.showBottleById.success"),url);
    }


    @PostMapping("/sendFriendRequest")
    @Operation(summary = "请求成为好友")
    public AjaxResult toBeFriends(@RequestBody FriendSendDTO friendSendDTO) {
        log.info("请求成为好友");
        bottleService.getBottle(friendSendDTO);
        return AjaxResult.success(MessageUtils.message("Friend.application.sent.success"));
    }

    @GetMapping("/ProcessingFriendRequests")
    @Operation(summary = "获取好友申请列表")
    public AjaxResult ProcessingFriendRequests() {
        log.info("处理好友申请");
        List<FriendRequestVO> friendRequests = friendService.ProcessingFriendRequests();
        if (friendRequests.isEmpty()) {
            return AjaxResult.success(MessageUtils.message("friend.friendRequest.empty"));
        } else {
            return AjaxResult.success(MessageUtils.message("friend.Processing.requests"),friendRequests);

        }
    }

    @PutMapping("/BecomeFriend")
    @Operation(summary = "同意成为好友")
    public AjaxResult BecomeFriend(@RequestBody BeFriendDTO beFriendDTO) {
        log.info("成为好友");
        String reply = friendService.becomeFriend(beFriendDTO);
        return AjaxResult.success(MessageUtils.message("Friend.application.success"),reply);
    }


    @PutMapping("/ThrowBack")
    @Operation(summary = "扔回海里")
    public AjaxResult ThrowBack() {
        log.info("扔回漂流瓶");
        bottleService.ThrowBack();
        return AjaxResult.success(MessageUtils.message("bottle.throw.back.success"));
    }






}

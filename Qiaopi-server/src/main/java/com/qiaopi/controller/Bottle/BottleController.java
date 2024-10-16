package com.qiaopi.controller.Bottle;


import com.qiaopi.dto.BeFriendDTO;
import com.qiaopi.dto.BottleGenDTO;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.FriendRequest;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.BottleService;
import com.qiaopi.service.FriendService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.BottleVo;
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
    @Operation(summary = "生成漂流瓶")
    public AjaxResult GenerateDriftBottle(@RequestBody BottleGenDTO bottleGenDTO) {

        log.info("生成漂流瓶：{}", bottleGenDTO);
        String url = bottleService.GenerateDriftBottle(bottleGenDTO);
        return AjaxResult.success(MessageUtils.message("bottle.generateImage.success"),url);
    }


    @GetMapping("/showDriftBottle")
    @Operation(summary = "展示漂流瓶")
    public AjaxResult showtBottleById() {
        log.info("展示漂流瓶");
        String url = bottleService.showBottle();
        return AjaxResult.success(MessageUtils.message("bottle.showBottleById.success"),url);
    }


    @GetMapping("/getDriftBottle")
    @Operation(summary = "捡走漂流瓶")
    public AjaxResult getBottleById() {
        log.info("捡走漂流瓶");
        BottleVo bottleVo = bottleService.getBottle();
        return AjaxResult.success(MessageUtils.message("bottle.getBottleById.success"),bottleVo);
    }


    @GetMapping("/sendFriendRequest")
    @Operation(summary = "请求成为好友")
    public AjaxResult sendFriendRequest(@RequestParam("bottleId") Long id) {
        log.info("请求成为好友");
        String reply = friendService.sendFriendRequest(id);
        return AjaxResult.success(MessageUtils.message("Friend.application.sent.success"),reply);
    }

    @GetMapping("/ProcessingFriendRequests")
    @Operation(summary = "处理好友申请")
    public AjaxResult ProcessingFriendRequests() {
        log.info("处理好友申请");
        List<FriendRequest> friendRequests = friendService.ProcessingFriendRequests();
        if (friendRequests.isEmpty()) {
            return AjaxResult.success(MessageUtils.message("friend.friendRequest.empty"));
        } else {
            return AjaxResult.success(MessageUtils.message("friend.Processing.requests"),friendRequests);

        }
    }

    @PostMapping("/BecomeFriend")
    @Operation(summary = "成为好友")
    public AjaxResult BecomeFriend(@RequestBody BeFriendDTO beFriendDTO) {
        log.info("成为好友");
        String reply = friendService.BecomeFriend(beFriendDTO);
        return AjaxResult.success(MessageUtils.message("Friend.application.success"),reply);
    }






}

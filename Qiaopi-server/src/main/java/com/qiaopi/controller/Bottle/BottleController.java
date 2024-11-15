package com.qiaopi.controller.Bottle;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.BeFriendDTO;
import com.qiaopi.dto.BottleGenDTO;
import com.qiaopi.dto.FriendSendDTO;
import com.qiaopi.entity.TaskTable;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.BottleService;
import com.qiaopi.service.FriendService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.FriendRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private final StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/generateDriftBottle")
    @Operation(summary = "写漂流瓶")
    public AjaxResult GenerateDriftBottle(@RequestBody BottleGenDTO bottleGenDTO) {
        Long userId = UserContext.getUserId();

        log.info("生成漂流瓶：{}", bottleGenDTO);
        String url = bottleService.generateDriftBottle(bottleGenDTO);

        //在这里获取当前线程用户的id 根据此id设置用于redis key
        LocalDateTime now = LocalDateTime.now();
        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String jsonStr = stringRedisTemplate.opsForValue().get(userKey);
        //通过这个键 获取导redis中的值 并且将这个值转化为TaskTable对象的集合
        try {
            // 将json字符串转换为TaskTable对象列表
            List<TaskTable> taskTableList = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, TaskTable.class));
            //获取taskTableList中id为1的对象
            TaskTable taskTable = taskTableList.stream().filter(t -> t.getId().equals(2L)).findFirst().orElse(null);
            //将对象中的 status 设为1
            taskTable.setStatus(1);
            //将修改后的对象重新转换为json字符串并存入redis
            stringRedisTemplate.opsForValue().set(userKey, objectMapper.writeValueAsString(taskTableList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return AjaxResult.success(MessageUtils.message("bottle.generateImage.success"),url);

    }


    @GetMapping("/showDriftBottle")
    @Operation(summary = "获取漂流瓶")
    public AjaxResult getBottle() {
        log.info("获取漂流瓶");
        try {
            Thread.sleep(500); // 睡眠1秒
        } catch (InterruptedException e) {
            log.error("线程睡眠中断", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
        }
        String url = bottleService.getBottle();
        return AjaxResult.success(MessageUtils.message("bottle.showBottleById.success"),url);
    }


    @PutMapping("/ThrowBack")
    @Operation(summary = "扔回")
    public AjaxResult ThrowBack() {
        log.info("扔回漂流瓶");
        bottleService.ThrowBack();
        return AjaxResult.success(MessageUtils.message("bottle.throw.back.success"));
    }


    @PostMapping("/sendFriendRequest")
    @Operation(summary = "请求成为好友")
    public AjaxResult toBeFriends(@RequestBody FriendSendDTO friendSendDTO) {
        log.info("请求成为好友");
        Long userId = UserContext.getUserId();
        bottleService.sendFriendRequest(friendSendDTO);
        //在这里获取当前线程用户的id 根据此id设置用于redis key
        LocalDateTime now = LocalDateTime.now();
        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String jsonStr = stringRedisTemplate.opsForValue().get(userKey);
        //通过这个键 获取导redis中的值 并且将这个值转化为TaskTable对象的集合
        try {
            // 将json字符串转换为TaskTable对象列表
            List<TaskTable> taskTableList = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, TaskTable.class));
            //获取taskTableList中id为1的对象
            TaskTable taskTable = taskTableList.stream().filter(t -> t.getId().equals(5L)).findFirst().orElse(null);
            //将对象中的 status 设为1
            taskTable.setStatus(1);
            //将修改后的对象重新转换为json字符串并存入redis
            stringRedisTemplate.opsForValue().set(userKey, objectMapper.writeValueAsString(taskTableList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


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




}

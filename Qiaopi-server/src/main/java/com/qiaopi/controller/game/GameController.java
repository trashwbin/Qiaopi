package com.qiaopi.controller.game;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiaopi.context.UserContext;
import com.qiaopi.entity.TaskTable;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.GameService;
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

import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/game")
@Slf4j
@Tag(name = "游戏相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GameController {

    private final GameService gameService;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/winFfl")
    @Operation(summary = "赢得翻翻乐")
    public AjaxResult list() {
        Long userId = UserContext.getUserId();
        log.info("赢得翻翻乐:{}",userId);
        gameService.winFfl(userId);

        //在这里获取当前线程用户的id 根据此id设置用于redis key
        LocalDateTime now = LocalDateTime.now();
        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String jsonStr = stringRedisTemplate.opsForValue().get(userKey);
        //通过这个键 获取导redis中的值 并且将这个值转化为TaskTable对象的集合
        try {
            // 将json字符串转换为TaskTable对象列表
            List<TaskTable> taskTableList = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, TaskTable.class));
            //获取taskTableList中id为1的对象
            TaskTable taskTable = taskTableList.stream().filter(t -> t.getId().equals(4L)).findFirst().orElse(null);
            //将对象中的 status 设为1
            taskTable.setStatus(1);
            //将修改后的对象重新转换为json字符串并存入redis
            stringRedisTemplate.opsForValue().set(userKey, objectMapper.writeValueAsString(taskTableList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }



        return AjaxResult.success(message("ffl.play.success"));
    }

    @GetMapping("/getFflLimit")
    @Operation(summary = "获翻翻乐次数限制")
    public AjaxResult getFflLimit() {
        Long userId = UserContext.getUserId();
        log.info("获翻翻乐次数限制:{}",userId);
        return AjaxResult.success(message("ffl.limit.success"), gameService.getFflLimit(userId));
    }
}



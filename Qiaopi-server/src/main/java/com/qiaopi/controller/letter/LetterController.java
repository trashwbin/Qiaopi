package com.qiaopi.controller.letter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.Letter;
import com.qiaopi.dto.LetterSendDTO;
import com.qiaopi.entity.TaskTable;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.LetterService;
import com.qiaopi.vo.LetterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/letter")
@Slf4j
@Tag(name = "书信相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LetterController {


    @Autowired
    private LetterService letterService;
    @Autowired
    private final StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    @PostMapping("/generateLetter")
    @Operation(summary = "生成侨批")
    public AjaxResult generateImage(@RequestBody LetterGenDTO letterGenDTO) {
        log.info("生成侨批：{}", letterGenDTO);
        //String url = letterService.generateImage(letterGenDTO);
        return AjaxResult.success(message("letter.generateImage.success"));
    }

    @GetMapping()
    @Operation(summary = "发信功能测试")
    public AjaxResult hi(){
        Letter letter = new Letter();
//        letter.setRecipientEmail("3348620049@qq.com");
        letter.setRecipientEmail("trashwbin@qq.com");
        List<Letter> letters= new ArrayList<>();
        letters.add(letter);
        letterService.sendLetterToEmail(letters);
        return AjaxResult.success("hi");
    }


    @PostMapping("/sendLetter")
    @Operation(summary = "生成封面并发送")
    public AjaxResult CoverGenerieren(@RequestBody LetterSendDTO letterSendDTO) {
        log.info("生成封面并发送：{}", letterSendDTO);
        Long userId = UserContext.getUserId();
        LetterVO letterVO = letterService.sendLetterPre(letterSendDTO);


        //在这里获取当前线程用户的id 根据此id设置用于redis key
        LocalDateTime now = LocalDateTime.now();
        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String jsonStr = stringRedisTemplate.opsForValue().get(userKey);
        //通过这个键 获取导redis中的值 并且将这个值转化为TaskTable对象的集合
        try {
            // 将json字符串转换为TaskTable对象列表
            List<TaskTable> taskTableList = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, TaskTable.class));
            //获取taskTableList中id为1的对象
            TaskTable taskTable = taskTableList.stream().filter(t -> t.getId().equals(1L)).findFirst().orElse(null);
            //将对象中的 status 设为1
            taskTable.setStatus(1);
            //将修改后的对象重新转换为json字符串并存入redis
            stringRedisTemplate.opsForValue().set(userKey, objectMapper.writeValueAsString(taskTableList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        return AjaxResult.success(message("letter.send.success"),letterVO);
    }

    @GetMapping("/getMySendLetter")
    @Operation(summary = "获取我写的侨批")
    public AjaxResult getMySendLetter() {

        return AjaxResult.success(message("letter.get.my.send.success"),letterService.getMySendLetter(UserContext.getUserId()));
    }

    //获取我收到的侨批
    @GetMapping("/getMyReceiveLetter")
    @Operation(summary = "获取我收到的侨批")
    public AjaxResult getMyReceiveLetter() {
        return AjaxResult.success(message("letter.get.my.receive.success"),letterService.getMyReceiveLetter(UserContext.getUserId()));
    }

    //获取我收到未读的侨批
    @GetMapping("/getMyNotReadLetter")
    @Operation(description = "或许首页可以弹出这个未读的侨批", summary = "获取我未读的侨批")
    public AjaxResult getMyNotReadLetter() {
        return AjaxResult.success(message("letter.get.my.receive.not.read.success"),letterService.getMyNotReadLetter(UserContext.getUserId()));
    }

    @PutMapping("/readLetter/{letterId}")
    @Operation(summary = "标记为已读")
    public AjaxResult readLetter(@PathVariable Long letterId) {
        letterService.readLetter(letterId);
        return AjaxResult.success(message("letter.read.success"));
    }




}



package com.qiaopi.controller.Question;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.QuestionSubmitDTO;
import com.qiaopi.entity.Questions;
import com.qiaopi.entity.TaskTable;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.QuestionService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.GenQuestionVO;
import com.qiaopi.vo.QuestionSubmitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/question")
@Slf4j
@Tag(name = "问题游戏相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/userLoginPage")
    @Operation(summary = "用户登入游戏页面")
    public AjaxResult userLoginPage() {
        log.info(("用户登入游戏页面"));
        ArrayList<Integer> gamePaperNeedToShowId = questionService.userLoginPage();
        return AjaxResult.success(MessageUtils.message("question.gamePaper.show.success"),gamePaperNeedToShowId);
    }


    @PostMapping("/startAnswer")
    @Operation(summary = "用户开始答题")
    public AjaxResult startAnswer(@RequestParam int setId) {
        log.info(("用户开始答题"));
        GenQuestionVO genQuestionVO = questionService.startAnswer(setId);
        return AjaxResult.success(MessageUtils.message("question.giveUser.success"),genQuestionVO);
    }


    @PostMapping("/submitAnswers")
    @Operation(summary = "用户提交答案")
    public AjaxResult submitAnswers(@RequestBody List<QuestionSubmitDTO> questionSubmitDTOs) {
        log.info(("用户提交答案"));
        QuestionSubmitVO questionSubmitVO = questionService.submitAnswers(questionSubmitDTOs);
        return AjaxResult.success(MessageUtils.message("question.judge.success"),questionSubmitVO);
    }


    @GetMapping("/allAnswerToFront")
    @Operation(summary = "将全部内容给前端")
    public AjaxResult allAnswerToFront(@RequestParam int setId) {
        Long userId = UserContext.getUserId();

        log.info(("将全部内容给前端"));
        String encryptedData = questionService.allAnswerToFront(setId);

        //在这里获取当前线程用户的id 根据此id设置用于redis key
        LocalDateTime now = LocalDateTime.now();
        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String jsonStr = stringRedisTemplate.opsForValue().get(userKey);
        //通过这个键 获取导redis中的值 并且将这个值转化为TaskTable对象的集合
        try {
            // 将json字符串转换为TaskTable对象列表
            List<TaskTable> taskTableList = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, TaskTable.class));
            //获取taskTableList中id为1的对象
            TaskTable taskTable = taskTableList.stream().filter(t -> t.getId().equals(3L)).findFirst().orElse(null);
            //将对象中的 status 设为1
            taskTable.setStatus(1);
            //将修改后的对象重新转换为json字符串并存入redis
            stringRedisTemplate.opsForValue().set(userKey, objectMapper.writeValueAsString(taskTableList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        return AjaxResult.success(MessageUtils.message("question.give.detail.suceess"),encryptedData);


    }


    @GetMapping("/decode")
    @Operation(summary = "解码")
    public AjaxResult decode(@RequestParam String answer) {
        log.info(("解码"));
        List<Questions> decode = questionService.decode(answer);
        return AjaxResult.success(MessageUtils.message("question.decode.success"),decode);
    }


}

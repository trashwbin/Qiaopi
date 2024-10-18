package com.qiaopi.controller.Question;


import com.qiaopi.dto.QuestionSubmitDTO;
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
import org.springframework.web.bind.annotation.*;

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

   /* @GetMapping("/genQuestion")
    @Operation(summary = "生成问题")
    public AjaxResult genQuestion(Long userChooseSetId) {
        log.info(("生成问题"));
        GenQuestionVO genQuestionVO = questionService.genQuestion(userChooseSetId);
        return AjaxResult.success(MessageUtils.message("question.generate.success"),genQuestionVO);
    }
*/

    @GetMapping("/userLoginPage")
    @Operation(summary = "用户登入游戏页面")
    public AjaxResult userLoginPage() {
        log.info(("用户登入游戏页面"));
        ArrayList<Integer> gamePaperNeedToShowId = questionService.userLoginPage();
        return AjaxResult.success(MessageUtils.message("question.gamePaper.show.success"),gamePaperNeedToShowId);
    }


    @PostMapping("/startAnswer")
    @Operation(summary = "用户开始答题")
    public AjaxResult startAnswer(@RequestParam("setId") int setId) {
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




}

package com.qiaopi.service;

import com.qiaopi.dto.QuestionSubmitDTO;
import com.qiaopi.entity.Questions;
import com.qiaopi.vo.GenQuestionVO;
import com.qiaopi.vo.QuestionSubmitVO;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public interface QuestionService {

    //GenQuestionVO genQuestion(Long userChooseSetId);


    ArrayList<Integer> userLoginPage();

    GenQuestionVO startAnswer(int setId);

    QuestionSubmitVO submitAnswers(List<QuestionSubmitDTO> questionSubmitDTOs);

    /**
     *将全部问题等内容返回给前端
     * @param setId
     */
    String allAnswerToFront(int setId);

    /**
     * 解码
     * @param answer
     * @return
     */
    List<Questions> decode(String answer);
}

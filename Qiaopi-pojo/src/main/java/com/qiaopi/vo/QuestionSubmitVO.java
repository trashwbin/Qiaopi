package com.qiaopi.vo;

import com.qiaopi.entity.Questions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 判断提交题目答案对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QuestionAnswerVO", description = "题目答案以及用户做题情况对象")

public class QuestionSubmitVO {

    //需要给用户返回什么？？
    //1、题目id
    //2、题目内容
    //2.1题目的四个选项
    //5.正确答案
    //4.用户选择的答案是否正确
    //3.用户选择的答案
    //6.解析

    /**
     * 题目答案以及用户做题情况对象
     */
    @Schema(description = "题目答案以及用户做题情况对象")
    private List<QuestionAnswerVO> questionAnswerVOs;

    /**
     * 获取到的猪仔钱
     */
    @Schema(description = "获取到的猪仔钱")
    private Integer pigMoney;

    /**
     * 若用户全部正确，需要返回下一套题的所有题目
     */
    private List<Questions> questions;

    /**
     *用户答对的题目数
     */
    private Integer correctAnswersNumber;


    public QuestionSubmitVO(List<QuestionAnswerVO> questionAnswerVOs, Integer pigMoney,Integer correctAnswersNumber) {
        this.questionAnswerVOs = questionAnswerVOs;
        this.pigMoney = pigMoney;
        this.correctAnswersNumber = correctAnswersNumber;
    }

}

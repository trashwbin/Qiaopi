package com.qiaopi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目答案对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QuestionAnswerVO", description = "题目答案以及用户做题情况对象")
public class QuestionAnswerVO {


    /**
     * 题目id
     */
    @Schema(description = "题目id")
    private Long questionId;

    /**
     * 题目内容
     */
    @Schema(description = "题目内容")
    private String content;

    /**
     * 选项A
     */
    @Schema(description = "选项A")
    private String optionA;

    /**
     * 选项B
     */
    @Schema(description = "选项B")
    private String optionB;

    /**
     * 选项C
     */
    @Schema(description = "选项C")
    private String optionC;

    /**
     * 选项D
     */
    @Schema(description = "选项D")
    private String optionD;

    /**
     * 正确答案
     */
    @Schema(description = "正确答案")
    private String correctAnswer;

    /**
     * 用户选择的答案
     */
    @Schema(description = "用户选择的答案")
    private String userAnswer;

    /**
     * 用户选择的答案是否正确
     */
    @Schema(description = "用户选择的答案是否正确")
    private boolean isCorrect;

    /**
     * 题目解析
     */
    @Schema(description = "题目解析")
    private String explanation;

    // Getters and Setters for all fields

}

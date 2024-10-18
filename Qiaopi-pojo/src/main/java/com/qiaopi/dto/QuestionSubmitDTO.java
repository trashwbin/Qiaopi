package com.qiaopi.dto;

import com.qiaopi.entity.Questions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

/**
 * 答案提交对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QuestionSubmitDTO", description = "答案提交对象")

public class QuestionSubmitDTO {


    /**
     * 题目ID
     */
    @Schema(description = "题目ID")
    private Long questionId;  // 题目 ID

    /**
     * 题目内容
     */
    @Schema(description = "题目内容")
    private String questionContent;

    /**
     * 用户选择的选项
     */
    @Schema(description = "用户选择的选项")
    private String selectedOption;

}

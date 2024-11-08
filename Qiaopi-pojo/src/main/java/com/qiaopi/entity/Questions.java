package com.qiaopi.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("questions")
public class Questions {

    @Schema(description = "主键")
//    @TableField(value = "id")
    private Long id;

    /**
     * 试题套Id
     */
    @Schema(description = "试题套Id")
    @TableField(value = "set_id")
    private Long setId;

    /**
     * 试题Id
     */
    @Schema(description = "试题Id")
    @TableField(value = "set_sequence_id")
    private Long setSequenceID;

    /**
     * 试题内容
     */
    @Schema(description = "试题内容")
    @TableField(value = "content")
    private String Content;

    /**
     * 选项a
     */
    @Schema(description = "选项a")
    @TableField(value = "option_a")
    private String optionA;


    /**
     * 选项b
     */
    @Schema(description = "选项b")
    @TableField(value = "option_b")
    private String optionB;


    /**
     * 选项c
     */
    @Schema(description = "选项c")
    @TableField(value = "option_c")
    private String optionC;

    /**
     * 选项d
     */
    @Schema(description = "选项d")
    @TableField(value = "option_d")
    private String optionD;

    /**
     * 正确答案
     */
    @Schema(description = "正确答案")
    @TableField(value = "correct_answer")
    private String correctAnswer;


    /**
     * 解析
     */
    @Schema(description = "解析")
    @TableField(value = "explanation")
    private String explanation;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField(value = "remark")
    private String remark;


}

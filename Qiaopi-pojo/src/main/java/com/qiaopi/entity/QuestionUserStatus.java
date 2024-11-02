package com.qiaopi.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qiaopi.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(autoResultMap = true)
public class QuestionUserStatus extends BaseEntity {

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 试题套1
     */
    @Schema(description = "试题套1")
    @TableField(value = "question_set_1_id")
    private int questionSet1;

    /**
     * 试题套2
     */
    @Schema(description = "试题套2")
    @TableField(value = "question_set_2_id")
    private int questionSet2;

    /**
     * 试题套3
     */
    @Schema(description = "试题套3")
    @TableField(value = "question_set_3_id")
    private int questionSet3;
    /**
     * 试题套4
     */
    @Schema(description = "试题套4")
    @TableField(value = "question_set_4_id")
    private int questionSet4;
    /**
     * 试题套5
     */
    @Schema(description = "试题套5")
    @TableField(value = "question_set_5_id")
    private int questionSet5;
    /**
     * 试题套6
     */
    @Schema(description = "试题套6")
    @TableField(value = "question_set_6_id")
    private int questionSet6;
    /**
     * 试题套7
     */
    @Schema(description = "试题套7")
    @TableField(value = "question_set_7_id")
    private int questionSet7;
    /**
     * 试题套8
     */
    @Schema(description = "试题套8")
    @TableField(value = "question_set_8_id")
    private int questionSet8;
    /**
     * 试题套9
     */
    @Schema(description = "试题套9")
    @TableField(value = "question_set_9_id")
    private int questionSet9;
    /**
     * 试题套10
     */
    @Schema(description = "试题套10")
    @TableField(value = "question_set_10_id")
    private int questionSet10;



}

package com.qiaopi.vo;

import com.qiaopi.entity.Questions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenQuestionVO {

    /**
     * 返回给用户的题目
     */
    private List<Questions> questions;



}

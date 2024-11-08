package com.qiaopi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信件生成对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "LetterGenDTO", description = "信件生成对象")
public class LetterGenDTO {



    /**
     * 寄件人的姓名
     */
    @Schema(description = "寄件人的姓名")
    private String senderName;

    /**
     * 收件人的姓名
     */
    @Schema(description = "收件人的姓名")
    private String recipientName;

    /**
     * 信的内容
     */
    @Schema(description = "信的内容")
    private String letterContent;

    /**
     * 字体ID
     */
    @Schema(description = "字体ID")
    private Long fontId;

    /**
     * 纸张ID
     */
    @Schema(description = "纸张ID")
    private Long paperId;

    /**
     * 字体颜色ID
     */
    @Schema(description = "字体颜色ID")
    private Long fontColorId;



    /**
     * 信件类型(1:竖版字体信件,2:横版信件)
     */
    @Schema(description = "信件类型(1:竖版字体信件,2:横版信件)")
    private int letterType;

}

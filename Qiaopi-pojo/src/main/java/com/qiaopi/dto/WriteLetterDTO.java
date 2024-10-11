package com.qiaopi.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "WriteLetterDTO", description = "写书信对象")
public class WriteLetterDTO {

    /**
     * 用户名
     */
    //必填项
    @Schema(description = "用户名" , required = true,example = "admin")
    private String username;











}

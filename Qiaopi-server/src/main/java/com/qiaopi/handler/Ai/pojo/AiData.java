package com.qiaopi.handler.Ai.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import static com.qiaopi.constant.AiConstant.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiData {

    @Schema(description = "消息类型")
    // 1.系统提示消息 2.用户指令 3.互动消息
    private Integer type;
    @Schema(description = "操作码")
    private String code;
    @Schema(description = "数据")
    private Object data;

    public static AiData getDoneMessage(){
        return new AiData(TYPE_SYSTEM, CODE_DONE,null);
    }
}

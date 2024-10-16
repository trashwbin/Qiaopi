package com.qiaopi.dto;


import com.qiaopi.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "FriendSendDTO", description = "申请成为好友DTO")
public class FriendSendDTO {

    /**
     * 提供自己的地址
     */
    @Schema(description = "提供自己的地址")
    private Address giveAddresss;


    /**
     * 好友地址
     */
    @Schema(description = "请求文本")
    private String context;

}

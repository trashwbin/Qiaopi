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
@Schema(name = "BeFriendDTO", description = "成为朋友处理对象")
public class BeFriendDTO {

    /**
     * 请求id
     */
    @Schema(description = "请求id")
    private Long requestId;

    /**
     * 是否同意
     */
    @Schema(description = "是否同意，1为同意，2为拒绝")
    private Long isAccepted;

}

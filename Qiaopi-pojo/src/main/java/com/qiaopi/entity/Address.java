package com.qiaopi.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Schema(description = "地址id")
    private Long id;
    @Schema(description = "国家id")
    private Long  countryId;
    @Schema(description = "详细地址")
    private String formattedAddress;
    @Schema(description = "经度")
    private Double longitude;
    @Schema(description = "纬度")
    private Double latitude;
    @Schema(description = "是否默认地址")
    private String isDefault;
}

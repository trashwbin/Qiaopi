package com.qiaopi.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "国家")
public class Country implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id; // 主键
    private String countryName; // 国家名称
    private String countryNameEnglish; // 国家英文名称
    private String capitalName; // 首都名称
    private Double capitalLongitude; // 首都经度
    private Double capitalLatitude; // 首都纬度
}

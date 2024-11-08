package com.qiaopi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "PageQueryDTO", description = "分页查询对象")
public class PageQueryDTO {
    private Long page = 1L;
    private Long limit = 20L;
}

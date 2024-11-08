package com.qiaopi.controller.marketing;


import com.qiaopi.dto.PageQueryDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.MarketingService;
import com.qiaopi.utils.ip.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.qiaopi.utils.MessageUtils.message;

@Slf4j
@RestController
@RequestMapping("/marketing")
@RequiredArgsConstructor
public class MarketingController {
    private final MarketingService marketingService;
    @GetMapping("/list")
    @Operation(summary = "查询营销商品")
    public AjaxResult list(PageQueryDTO pageQueryDTO) {
        return AjaxResult.success(message("marketing.get.list.success"),marketingService.list(pageQueryDTO));
    }
}

package com.qiaopi.controller.font;


import com.qiaopi.context.UserContext;
import com.qiaopi.dto.FunctionCardUseDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.CardService;
import com.qiaopi.service.FontService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/font")
@Slf4j
@Tag(name = "字体相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FontController {

    private final FontService fontService;

    @GetMapping("/list")
    @Operation(summary = "获取字体商城列表")
    public AjaxResult list() {
        log.info("用户：{}获取字体商城列表", UserContext.getUserId());
        return AjaxResult.success(message("font.list.success"),fontService.list());
    }
    @GetMapping("/listColor")
    @Operation(summary = "获取字体颜色商城列表")
    public AjaxResult listColor() {
        log.info("用户：{}获取字体颜色商城列表", UserContext.getUserId());
        return AjaxResult.success(message("font.color.list.success"),fontService.listColor());
    }

    @PostMapping("/buyFont")
    @Operation(summary = "购买字体")
    public AjaxResult buyFont(@RequestParam Long fontId) {
        log.info("用户：{}购买字体：{}",UserContext.getUserId(), fontId);
        fontService.buyFont(fontId);
        return AjaxResult.success(message("font.buyFont.success"));
    }
    @PostMapping("/buyFontColor")
    @Operation(summary = "购买字体颜色")
    public AjaxResult buyFontColor(@RequestParam Long fontColorId) {
        log.info("用户：{}购买字体颜色：{}",UserContext.getUserId(), fontColorId);
        fontService.buyFontColor(fontColorId);
        return AjaxResult.success(message("font.color.buyFontColor.success"));
    }
}



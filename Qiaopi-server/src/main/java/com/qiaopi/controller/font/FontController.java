package com.qiaopi.controller.font;


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
        return AjaxResult.success(message("font.list.success"),fontService.list());
    }
    @GetMapping("/listColor")
    @Operation(summary = "获取字体颜色商城列表")
    public AjaxResult listColor() {
        return AjaxResult.success(message("font.color.list.success"),fontService.listColor());
    }

}



package com.qiaopi.controller.paper;


import com.qiaopi.context.UserContext;
import com.qiaopi.dto.FunctionCardUseDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.CardService;
import com.qiaopi.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/paper")
@Slf4j
@Tag(name = "纸张相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaperController {

    private final PaperService paperService;
    @GetMapping("/list")
    @Operation(summary = "获取纸张商城列表")
    public AjaxResult list() {
        log.info("获取纸张商城列表");
        return AjaxResult.success(message("paper.list.success"), paperService.list());
    }

    @PostMapping("/buyPaper")
    @Operation(summary = "购买纸张")
    public AjaxResult buyPaper(@RequestParam Long paperId) {
        log.info("用户{},购买纸张：{}", UserContext.getUserId(),paperId);
        paperService.buyPaper(paperId);
        return AjaxResult.success(message("paper.buyPaper.success"));
    }

    @GetMapping("/getFontPaperLimit")
    @Operation(summary = "获取字体纸张限制")
    public AjaxResult getFontPaperLimit() {
        log.info("获取字体纸张限制");
        return AjaxResult.success(message("paper.getFontPaperLimit.success"), paperService.getFontPaperLimit());
    }
}



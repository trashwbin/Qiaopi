package com.qiaopi.controller.card;


import com.qiaopi.dto.FunctionCardUseDTO;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.dto.LetterSendDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.CardService;
import com.qiaopi.service.LetterService;
import com.qiaopi.vo.LetterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/card")
@Slf4j
@Tag(name = "功能卡相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CardController {

    private final CardService cardService;
    @PutMapping("/useCard")
    @Operation(summary = "使用功能卡")
    public AjaxResult useCard(@RequestBody FunctionCardUseDTO functionCardUseDTO) {
        log.info("使用功能卡：{}", functionCardUseDTO);
        return AjaxResult.success(message("card.useCard.success"), cardService.useCard(functionCardUseDTO));
    }
    @GetMapping("/list")
    @Operation(summary = "获取功能卡商城列表")
    public AjaxResult list() {
        log.info("获取功能卡商城列表");
        return AjaxResult.success(message("card.list.success"),cardService.list());
    }
    @PostMapping("/buyCard")
    @Operation(summary = "购买功能卡")
    public AjaxResult buyCard(@RequestParam Long cardId) {
        log.info("购买功能卡：{}", cardId);
        cardService.buyCard(cardId);
        return AjaxResult.success(message("card.buyCard.success"));
    }
}



package com.qiaopi.controller.game;


import com.qiaopi.context.UserContext;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/game")
@Slf4j
@Tag(name = "游戏相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GameController {

    private final GameService gameService;
    @GetMapping("/winFfl")
    @Operation(summary = "赢得翻翻乐")
    public AjaxResult list() {
        Long userId = UserContext.getUserId();
        log.info("赢得翻翻乐:{}",userId);
        gameService.winFfl(userId);
        return AjaxResult.success(message("ffl.play.success"));
    }

    @GetMapping("/getFflLimit")
    @Operation(summary = "获翻翻乐次数限制")
    public AjaxResult getFflLimit() {
        Long userId = UserContext.getUserId();
        log.info("获翻翻乐次数限制:{}",userId);
        return AjaxResult.success(message("ffl.limit.success"), gameService.getFflLimit(userId));
    }
}



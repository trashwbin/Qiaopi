package com.qiaopi.controller.paper;


import com.qiaopi.dto.FunctionCardUseDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.CardService;
import com.qiaopi.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/card")
@Slf4j
@Tag(name = "纸张相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaperController {

    private final PaperService paperService;
    @PutMapping("/List")
    @Operation(summary = "获取纸张商城列表")
    public AjaxResult list() {
        log.info("获取纸张商城列表");
        return AjaxResult.success(message("paper.list.success"), paperService.list());
    }

}



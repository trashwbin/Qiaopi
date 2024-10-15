package com.qiaopi.controller.Bottle;


import com.qiaopi.dto.BottleGenDTO;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.BottleService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.BottleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bottle")
@Slf4j
@Tag(name = "漂流瓶相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BottleController {

    @Autowired
    private BottleService bottleService;


    @PostMapping("/generateDriftBottle")
    @Operation(summary = "生成漂流瓶")
    public AjaxResult GenerateDriftBottle(@RequestBody BottleGenDTO bottleGenDTO) {

        log.info("生成漂流瓶：{}", bottleGenDTO);
        String url = bottleService.GenerateDriftBottle(bottleGenDTO);
        return AjaxResult.success(MessageUtils.message("bottle.generateImage.success"),url);
    }


    @GetMapping("/showDriftBottle")
    @Operation(summary = "展示漂流瓶")
    public AjaxResult showtBottleById() {
        log.info("展示漂流瓶");
        String url = bottleService.showBottle();
        return AjaxResult.success(MessageUtils.message("bottle.getBottleById.success"),url);
    }


    @GetMapping("/getDriftBottle")
    @Operation(summary = "捡走漂流瓶")
    public AjaxResult getBottleById() {
        log.info("捡走漂流瓶");
        BottleVo bottleVo = bottleService.getBottle();//TODO
        return AjaxResult.success(MessageUtils.message("bottle.getBottleById.success"),bottleVo);//TODO
    }







}

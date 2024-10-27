package com.qiaopi.controller.letter;


import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.Letter;
import com.qiaopi.dto.LetterSendDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.LetterService;
import com.qiaopi.vo.LetterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/letter")
@Slf4j
@Tag(name = "书信相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LetterController {


    @Autowired
    private LetterService letterService;

    @PostMapping("/generateLetter")
    @Operation(summary = "生成侨批")
    public AjaxResult generateImage(@RequestBody LetterGenDTO letterGenDTO) {
        log.info("生成侨批：{}", letterGenDTO);
        //String url = letterService.generateImage(letterGenDTO);
        return AjaxResult.success(message("letter.generateImage.success"));
    }

    @GetMapping()
    @Operation(summary = "发信功能测试")
    public AjaxResult hi(){
        Letter letter = new Letter();
//        letter.setRecipientEmail("3348620049@qq.com");
        letter.setRecipientEmail("trashwbin@qq.com");
        List<Letter> letters= new ArrayList<>();
        letters.add(letter);
        letterService.sendLetterToEmail(letters);
        return AjaxResult.success("hi");
    }


    @PostMapping("/sendLetter")
    @Operation(summary = "生成封面并发送")
    public AjaxResult CoverGenerieren(@RequestBody LetterSendDTO letterSendDTO) throws IOException {
        log.info("生成封面并发送：{}", letterSendDTO);
        LetterVO letterVO = letterService.sendLetterPre(letterSendDTO);
        return AjaxResult.success(message("letter.send.success"),letterVO);
    }

    @GetMapping("/getMySendLetter")
    @Operation(summary = "获取我写的侨批")
    public AjaxResult getMySendLetter() {
        return AjaxResult.success(message("letter.get.my.send.success"),letterService.getMySendLetter());
    }

    //获取我收到的侨批
    @GetMapping("/getMyReceiveLetter")
    @Operation(summary = "获取我收到的侨批")
    public AjaxResult getMyReceiveLetter() {
        return AjaxResult.success(message("letter.get.my.receive.success"),letterService.getMyReceiveLetter());
    }

    //获取我收到未读的侨批
    @GetMapping("/getMyNotReadLetter")
    @Operation(description = "或许首页可以弹出这个未读的侨批", summary = "获取我未读的侨批")
    public AjaxResult getMyNotReadLetter() {
        return AjaxResult.success(message("letter.get.my.receive.not.read.success"),letterService.getMyNotReadLetter());
    }

    @PutMapping("/readLetter/{letterId}")
    @Operation(summary = "标记为已读")
    public AjaxResult readLetter(@PathVariable Long letterId) {
        letterService.readLetter(letterId);
        return AjaxResult.success(message("letter.read.success"));
    }




}



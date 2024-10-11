package com.qiaopi.controller.letter;


import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.dto.LetterSendDTO;
import com.qiaopi.mapper.FontColorMapper;
import com.qiaopi.mapper.FontMapper;
import com.qiaopi.mapper.PaperMapper;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.LetterService;
import com.qiaopi.utils.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequestMapping("/letter")
@Slf4j
@Tag(name = "书信相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LetterController {


    @Autowired
    private LetterService letterService;

    @PostMapping("/generateLetter")
    @Operation(summary = "生成字体照片")
    public AjaxResult generateImage(@RequestBody LetterGenDTO letterGenDTO) {
        log.info("生成字体照片：{}", letterGenDTO);
        String url = letterService.generateImage(letterGenDTO);
        return AjaxResult.success(MessageUtils.message("letter.generateImage.success"),url);
    }



    @PostMapping("/sender")
    @Operation(summary = "生成封面照片")
    public AjaxResult CoverGenerieren(@RequestBody LetterSendDTO letterSendDTO) throws IOException {
        log.info("生成封面照片：{}", letterSendDTO);
        String url = letterService.coverGenerieren(letterSendDTO);

        return AjaxResult.success(MessageUtils.message("letter.generateImage.success"),url);//TODO 返回网址
    }








}



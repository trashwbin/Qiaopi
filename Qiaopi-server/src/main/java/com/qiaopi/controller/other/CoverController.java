package com.qiaopi.controller.other;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@Tag(name = "封面接口")
@Slf4j
public class CoverController {
    @GetMapping("/CoverImage")
    @Operation(summary = "生成字体照片")
    public ResponseEntity<byte[]> generateImage(
            //接口需要传入寄信人，寄信地址，收信人，收信地址
            @RequestParam String sender,  //寄信人
            @RequestParam String mailingAddress, // 寄信地址
            @RequestParam String Recipient, // 收信人
            @RequestParam String insideAddress  // 收信地址
    ) throws IOException {

        // 设置图片的宽和高（根据实际需求可以动态调整）
        int width = 1500; // 图片宽度
        int height = 1000; // 图片高度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像



        return null;
    }







}

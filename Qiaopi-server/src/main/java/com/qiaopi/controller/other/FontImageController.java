package com.qiaopi.controller.other;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RestController
public class FontImageController {

    @GetMapping("/generateImage")
    public ResponseEntity<byte[]> generateImage(
            @RequestParam String text,
            @RequestParam String font, // 字体文件名
            @RequestParam int fsize,   // 字体大小
            @RequestParam String color // 字体颜
    ) throws IOException {
// 设置图片的宽和高（根据实际需求可以动态调整）
        int width = 800; // 图片宽度
        int height = 200; // 图片高度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像
        
        // 加载全透明背景图片
        BufferedImage bgImage = ImageIO.read(new File("D:\\A_IDE\\Code\\items\\qiaopipre\\Qiaopi-server\\src\\main\\resources\\images\\bg.png"));
//        背景图适配绘制
        g2d.drawImage(bgImage,0,0,width,height,null);

        // 加载自定义字体
        Font customFont; // 定义字体对象
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("D:\\A_IDE\\Code\\items\\qiaopipre\\Qiaopi-server\\src\\main\\resources\\fonts\\" + font)).deriveFont((float) fsize); // 尝试加载自定义字体并设置字体大小
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); // 获取本地图形环境
            ge.registerFont(customFont); // 注册自定义字体
        } catch (FontFormatException | IOException e) {
            // 如果字体加载失败，使用默认字体
            customFont = new Font("宋体", Font.PLAIN, fsize); // 使用支持中文的默认字体，例如宋体
        }

        // 设置字体及颜色
        g2d.setFont(customFont); // 设置字体
        g2d.setColor(Color.decode("#" + color)); // 设置字体颜色

        // 绘制文本
        FontMetrics fontMetrics = g2d.getFontMetrics(); // 获取字体度量信息
        int textWidth = fontMetrics.stringWidth(text); // 计算文本宽度
        int textHeight = fontMetrics.getHeight(); // 计算文本高度
        int x = (width - textWidth) / 2; // 计算水平居中的X坐标
        int y = (height - textHeight) / 2 + fontMetrics.getAscent(); // 计算垂直居中的Y坐标

        g2d.drawString(text, x, y); // 绘制文本

// 释放图形资源
        g2d.dispose(); // 释放Graphics2D对象

// 将图片写入字节流
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // 创建字节数组输出流
        ImageIO.write(bufferedImage, "png", baos); // 将BufferedImage写入字节数组输出流
        byte[] imageBytes = baos.toByteArray(); // 获取字节数组

// 设置响应头并返回图片
        HttpHeaders headers = new HttpHeaders(); // 创建HttpHeaders对象
        headers.setContentType(MediaType.IMAGE_PNG); // 设置响应内容类型为PNG图片
        headers.setContentLength(imageBytes.length); // 设置响应内容长度

        return ResponseEntity.ok().headers(headers).body(imageBytes); // 返回包含图片字节数组的响应实体
    }
}

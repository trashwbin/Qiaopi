package com.qiaopi.controller.other;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RestController
@Tag(name = "字体接口")
@Slf4j
public class FontImageController {
    @GetMapping("/generateImage")
    @Operation(summary = "生成字体照片")
    public ResponseEntity<byte[]> generateImage(
            @RequestParam String text,  //文本
            @RequestParam String font, // 字体文件名
            @RequestParam String color, // 字体颜
            @RequestParam String stationery// 信纸类型
    ) throws IOException {

        // 设置图片的宽和高（根据实际需求可以动态调整）
        int width = 1000; // 图片宽度
        int height = 1500; // 图片高度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像

        //进行判断,匹配不同的信纸
        //x调整左右位置，增右减左  y调整上下位置，增下减上
        if (stationery.equals("1")) {
            //第一种信纸
            Try(g2d,text, width, height, color, font,stationery,180,75);//调用数值使得文本与信纸对齐
        } else if (stationery.equals("2")) {
            //第二种信纸
            Try(g2d,text, width, height, color, font,stationery,172,140);
        } else if (stationery.equals("3")) {
            //第三种信纸
            Try(g2d,text, width, height, color, font,stationery,145,50);
        } else if (stationery.equals("4")) {
            //第四种信纸

        } else {
            //第五种信纸
        }

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


    /**
     * 绘制逆时针旋转90度的文本
     *
     * @param g2d   Graphics2D 对象
     * @param text  需要绘制的文本
     * @param x     文本起始绘制位置的x坐标
     * @param y     文本起始绘制位置的y坐标
     */
    private static void drawRotatedText(Graphics2D g2d, String text, int x, int y) {
        int charsPerLine = 15;
        int currentX = x;
        int currentY = y;

        FontMetrics fontMetrics = g2d.getFontMetrics();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 创建一个新的 AffineTransform
            AffineTransform affineTransform = new AffineTransform();

            // 平移变换到当前字符的中心
            affineTransform.translate(currentX + fontMetrics.charWidth(c) / 2, currentY + fontMetrics.getHeight() / 2);

            // 逆时针旋转90度
            affineTransform.rotate(-Math.PI / 2, 0, 0);

            // 反向平移回到原点
            affineTransform.translate(-(currentX + fontMetrics.charWidth(c) / 2), -(currentY + fontMetrics.getHeight() / 2));

            // 应用变换并绘制字符
            g2d.setTransform(affineTransform);
            g2d.drawString(String.valueOf(c), currentX, currentY);

            // 更新 x 坐标以便绘制下一个字符
            currentX += fontMetrics.charWidth(c);

            // 检查是否需要换行
            if ((i + 1) % charsPerLine == 0 && i < text.length() - 1) {
                // 重置 x 坐标
                currentX = x;

                // 更新 y 坐标
                currentY += fontMetrics.getHeight();
            }
        }
    }


    /**
     * 绘制逆时针旋转90度的文本
     *
     * @param g2d   Graphics2D 对象
     * @param text  需要绘制的文本
     * @param width  图片高度
     * @param height  图片宽度
     * @param color  字体颜色
     * @param font  字体大小
     * @param stationery  信纸类型
     * @param x     文本起始绘制位置的x坐标
     * @param y     文本起始绘制位置的y坐标
     */
    private static void Try(Graphics2D g2d,String text, int width, int height,String color,String font,String stationery,int x,int y) throws IOException {

        // 加载书信图片
        BufferedImage bgImage = ImageIO.read(new File("D:\\Code\\QiaoPi\\qiaopi\\Qiaopi-server\\src\\main\\resources\\images\\Stationery\\0"+stationery+".png"));

        //背景图适配绘制
        g2d.drawImage(bgImage,0,0,width,height,null);

        // 加载自定义字体
        Font customFont; // 定义字体对象
        try {
            // 构建字体文件路径
            String fontPath = "D:\\Code\\QiaoPi\\qiaopi\\Qiaopi-server\\src\\main\\resources\\fonts\\MainContent\\" + font;

            // 加载字体文件
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath)).deriveFont((float) 50);

            // 获取本地图形环境并注册字体
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

        } catch (FontFormatException | IOException e) {
            // 如果字体加载失败，使用默认字体
            customFont = new Font("宋体", Font.PLAIN, 50); // 使用支持中文的默认字体，例如宋体
        }

        // 设置字体及颜色
        g2d.setFont(customFont); // 设置字体
        g2d.setColor(Color.decode("#" + color)); // 设置字体颜色

        // 获取字体度量信息
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // 计算文本宽度和高度
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        drawRotatedText(g2d, text, x, y);

        // 释放图形资源
        g2d.dispose(); // 释放Graphics2D对象
    }



}






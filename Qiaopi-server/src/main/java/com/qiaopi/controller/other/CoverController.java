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
@Tag(name = "封面接口")
@Slf4j
public class CoverController {
    @GetMapping("/CoverImage")
    @Operation(summary = "生成封面照片")
    public ResponseEntity<byte[]> generateImage(
            //接口需要传入寄信人，寄信地址，收信人，收信地址
            @RequestParam String sender,  //寄信人
            @RequestParam String mailingAddress, // 寄信地址
            @RequestParam String Recipient, // 收信人
            @RequestParam String insideAddress  // 收信地址

    ) throws IOException {

        // 设置图片的宽和高（根据实际需求可以动态调整）
        int width = 1000; // 图片宽度
        int height = 600; // 图片高度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像


        //x调整左右位置，增右减左  y调整上下位置，增下减上
        //寄信地址 mailingAddress
        drawMain(g2d,mailingAddress,width,height,400,400);
        //寄信人  sender 姜峰勇
        drawSubordinate(g2d,sender,width,height,200,50);
        //收信地址  insideAddress 广东省汕头市
        drawSubordinate(g2d,insideAddress,width,height,400,215);
        //收信人  Recipient 郭灿衡
        drawSubordinate(g2d,Recipient,width,height,700,400);


        g2d.dispose();

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
    private static void Main(Graphics2D g2d, String text, int x, int y) {
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
     * @param g2d        Graphics2D 对象
     * @param text       需要绘制的文本
     * @param width      图片高度
     * @param height     图片宽度
     * @param x          文本起始绘制位置的x坐标
     * @param y          文本起始绘制位置的y坐标
     */
    private static void drawMain(Graphics2D g2d, String text, int width, int height, int x, int y) throws IOException {
        // 加载书信图片
        BufferedImage bgImage = ImageIO.read(new File("Qiaopi-server\\src\\main\\resources\\images\\Cover\\Cover.png"));

        //背景图适配绘制
        g2d.drawImage(bgImage, 0, 0, width, height, null);

        // 加载自定义字体
        Font customFont; // 定义字体对象
        try {
            // 构建字体文件路径
            String fontPath = "Qiaopi-server\\src\\main\\resources\\fonts\\CoverFont\\晨光大字.TTF";

            // 加载字体文件
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath)).deriveFont((float) 180);

            // 获取本地图形环境并注册字体
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

        } catch (FontFormatException | IOException e) {
            // 如果字体加载失败，使用默认字体
            customFont = new Font("宋体", Font.PLAIN, 180); // 使用支持中文的默认字体，例如宋体
        }

        // 设置字体及颜色
        g2d.setFont(customFont); // 设置字体
        g2d.setColor(Color.decode("#000000")); // 设置字体颜色

        // 获取字体度量信息
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // 计算文本宽度和高度
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        Main(g2d, text, x, y);

        /*// 释放图形资源
        g2d.dispose(); // 释放Graphics2D对象*/
    }



    private static void Subordinate(Graphics2D g2d, String text, int x, int y) {
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
     * @param g2d        Graphics2D 对象
     * @param text       需要绘制的文本
     * @param width      图片高度
     * @param height     图片宽度
     * @param x          文本起始绘制位置的x坐标
     * @param y          文本起始绘制位置的y坐标
     */
    private static void drawSubordinate(Graphics2D g2d, String text, int width, int height, int x, int y) throws IOException {
        /*// 加载书信图片
        BufferedImage bgImage = ImageIO.read(new File("D:\\Code\\QiaoPi\\qiaopi\\Qiaopi-server\\src\\main\\resources\\images\\Cover\\Cover.png"));

        //背景图适配绘制
        g2d.drawImage(bgImage, 0, 0, width, height, null);*/

        // 加载自定义字体
        Font customFont; // 定义字体对象
        try {
            // 构建字体文件路径
            String fontPath = "D:\\Code\\QiaoPi\\qiaopi\\Qiaopi-server\\src\\main\\resources\\fonts\\CoverFont\\晨光大字.TTF";

            // 加载字体文件
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath)).deriveFont((float) 100);

            // 获取本地图形环境并注册字体
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

        } catch (FontFormatException | IOException e) {
            // 如果字体加载失败，使用默认字体
            customFont = new Font("宋体", Font.PLAIN, 180); // 使用支持中文的默认字体，例如宋体
        }

        // 设置字体及颜色
        g2d.setFont(customFont); // 设置字体
        g2d.setColor(Color.decode("#000000")); // 设置字体颜色

        // 获取字体度量信息
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // 计算文本宽度和高度
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        Subordinate(g2d, text, x, y);

        /*// 释放图形资源
        g2d.dispose(); // 释放Graphics2D对象*/
    }









}

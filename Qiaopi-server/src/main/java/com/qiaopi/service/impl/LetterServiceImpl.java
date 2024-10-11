package com.qiaopi.service.impl;

import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.FontColor;
import com.qiaopi.entity.Paper;
import com.qiaopi.mapper.FontColorMapper;
import com.qiaopi.mapper.FontMapper;
import com.qiaopi.mapper.PaperMapper;
import com.qiaopi.service.LetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;


@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class LetterServiceImpl implements LetterService {

    @Autowired
    private FontColorMapper fontColorMapper;

    @Autowired
    private FontMapper fontMapper;

    @Autowired
    private PaperMapper paperMapper;


    @Override
    public void Main(Graphics2D g2d, String text, int x, int y) {
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

    @Override
    public  void drawMain(Graphics2D g2d, String text, int width, int height, String color, String font, String stationery, int x, int y) throws IOException {
        // 加载书信图片
        //BufferedImage bgImage = ImageIO.read(new File("Qiaopi-server\\src\\main\\resources\\images\\Stationery\\0"+stationery+".png"));
        BufferedImage bgImage = ImageIO.read(new File("D:\\Code\\QiaoPi\\qiaopi\\Qiaopi-server\\src\\main\\resources\\images\\Stationery\\"+stationery+".png"));


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
        g2d.setColor(Color.decode(color)); // 设置字体颜色

        // 获取字体度量信息
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // 计算文本宽度和高度
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        Main(g2d, text, x, y);

        // 释放图形资源
        //g2d.dispose(); // 释放Graphics2D对象
    }

    @Override
    public void Sender(Graphics2D g2d, String sender, int x, int y) {
        int charsPerLine = 15;
        int currentX = x;
        int currentY = y;

        FontMetrics fontMetrics = g2d.getFontMetrics();

        for (int i = 0; i < sender.length(); i++) {
            char c = sender.charAt(i);

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
            if ((i + 1) % charsPerLine == 0 && i < sender.length() - 1) {
                // 重置 x 坐标
                currentX = x;

                // 更新 y 坐标
                currentY += fontMetrics.getHeight();
            }
        }
    }

    @Override
    public void drawSender(Graphics2D g2d, String sender, int width, int height, String color, String font, String stationery, int x, int y) throws IOException {
        // 加载书信图片
        BufferedImage bgImage = ImageIO.read(new File("D:\\Code\\QiaoPi\\qiaopi\\Qiaopi-server\\src\\main\\resources\\images\\Stationery\\"+stationery+".png"));

        /*//背景图适配绘制
        g2d.drawImage(bgImage,0,0,width,height,null);*/

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
        g2d.setColor(Color.decode(color)); // 设置字体颜色

        // 获取字体度量信息
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // 计算文本宽度和高度
        int textWidth = fontMetrics.stringWidth(sender);
        int textHeight = fontMetrics.getHeight();

        Sender(g2d, sender, x, y);

        // 释放图形资源
        //g2d.dispose(); // 释放Graphics2D对象
    }

    @Override
    public void Recipient(Graphics2D g2d, String recipient, int x, int y) {
        int charsPerLine = 15;
        int currentX = x;
        int currentY = y;

        FontMetrics fontMetrics = g2d.getFontMetrics();

        for (int i = 0; i < recipient.length(); i++) {
            char c = recipient.charAt(i);

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
            if ((i + 1) % charsPerLine == 0 && i < recipient.length() - 1) {
                // 重置 x 坐标
                currentX = x;

                // 更新 y 坐标
                currentY += fontMetrics.getHeight();
            }
        }
    }

    @Override
    public void drawRecipient(Graphics2D g2d, String recipient, int width, int height, String color, String font, String stationery, int x, int y) throws IOException {

        // 加载书信图片
        BufferedImage bgImage = ImageIO.read(new File("D:\\Code\\QiaoPi\\qiaopi\\Qiaopi-server\\src\\main\\resources\\images\\Stationery\\"+stationery+".png"));

        /*//背景图适配绘制
        g2d.drawImage(bgImage,0,0,width,height,null);*/

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
        g2d.setColor(Color.decode(color)); // 设置字体颜色

        // 获取字体度量信息
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // 计算文本宽度和高度
        int textWidth = fontMetrics.stringWidth(recipient);
        int textHeight = fontMetrics.getHeight();

        Recipient(g2d, recipient, x, y);

        // 释放图形资源
        //g2d.dispose(); // 释放Graphics2D对象
    }



    @Override
    public ArrayList<String> generationParameters(LetterGenDTO letterGenDTO) {
        String sender = letterGenDTO.getSenderName(); //寄件人的姓名
        String recipient = letterGenDTO.getRecipientName();//收件人的姓名
        String text = letterGenDTO.getLetterContent();//信的内容

        com.qiaopi.entity.Font tmepFont = fontMapper.selectById(letterGenDTO.getFontId());
        String font = tmepFont.getName();

        Paper paper = paperMapper.selectById(letterGenDTO.getPaperId());
        String stationery = paper.getName();

        FontColor fontColor = fontColorMapper.selectById(letterGenDTO.getFontId());
        String color = fontColor.getHexCode();

        return new ArrayList<>(Arrays.asList(sender, recipient, text, font, color, stationery));
    }



    @Override
    public BufferedImage rotateImage(BufferedImage originalImage, int degrees) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 计算旋转后的图像尺寸
        int newWidth = Math.max(width, height);
        int newHeight = Math.max(width, height);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = rotatedImage.createGraphics();
        AffineTransform at = new AffineTransform();

        // 平移至中心
        at.translate(newWidth / 2.0, newHeight / 2.0);
        // 旋转
        at.rotate(Math.toRadians(degrees));
        // 平移回原点
        at.translate(-width / 2.0, -height / 2.0);

        g2d.setTransform(at);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
    }



}




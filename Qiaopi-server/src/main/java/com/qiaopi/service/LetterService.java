package com.qiaopi.service;

import com.qiaopi.dto.LetterGenDTO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public interface LetterService {

    /**
     * 绘制逆时针旋转90度的文本
     *
     * @param g2d   Graphics2D 对象
     * @param text  需要绘制的文本
     * @param x     文本起始绘制位置的x坐标
     * @param y     文本起始绘制位置的y坐标
     */
    void Main(Graphics2D g2d, String text, int x, int y);

    /**
     * 绘制逆时针旋转90度的文本，并设置背景图片、字体和颜色
     *
     * @param g2d        Graphics2D 对象
     * @param text       需要绘制的文本
     * @param width      图片宽度
     * @param height     图片高度
     * @param color      字体颜色
     * @param font       字体大小
     * @param stationery 信纸类型
     * @param x          文本起始绘制位置的x坐标
     * @param y          文本起始绘制位置的y坐标
     */
    void drawMain(Graphics2D g2d, String text, int width, int height, String color, String font, String stationery, int x, int y) throws IOException;

    /**
     * 绘制逆时针旋转90度的文本
     *
     * @param g2d Graphics2D 对象
     * @param sender 需要绘制的文本
     * @param x 文本起始绘制位置的x坐标
     * @param y 文本起始绘制位置的y坐标
     */
    void Sender(Graphics2D g2d, String sender, int x, int y);

    /**
     * 绘制逆时针旋转90度的文本，并设置背景图片、字体和颜色
     * @param g2d        Graphics2D 对象
     * @param sender     需要绘制的文本
     * @param width      图片宽度
     * @param height     图片高度
     * @param color      字体颜色
     * @param font       字体大小
     * @param stationery 信纸类型
     * @param x          文本起始绘制位置的x坐标
     * @param y          文本起始绘制位置的y坐标
     */
    void drawSender(Graphics2D g2d, String sender, int width, int height, String color, String font, String stationery, int x, int y) throws IOException;

    /**
     * 绘制逆时针旋转90度的文本
     *
     * @param g2d   Graphics2D 对象
     * @param recipient 需要绘制的文本
     * @param x     文本起始绘制位置的x坐标
     * @param y     文本起始绘制位置的y坐标
     */
    void Recipient(Graphics2D g2d, String recipient, int x, int y);

    /**
     * 绘制逆时针旋转90度的文本，并设置背景图片、字体和颜色
     *
     * @param g2d        Graphics2D 对象
     * @param recipient  需要绘制的文本
     * @param width      图片宽度
     * @param height     图片高度
     * @param color      字体颜色
     * @param font       字体大小
     * @param stationery 信纸类型
     * @param x          文本起始绘制位置的x坐标
     * @param y          文本起始绘制位置的y坐标
     */
    void drawRecipient(Graphics2D g2d, String recipient, int width, int height, String color, String font, String stationery, int x, int y) throws IOException ;


    /**
     * 生成参数
     * @param letterGenDTO
     * @return
     */
    ArrayList<String> generationParameters(LetterGenDTO letterGenDTO);


    /**
     * 旋转图片
     * @param originalImage
     * @param degrees
     * @return
     */
    BufferedImage rotateImage(BufferedImage originalImage, int degrees);





    }





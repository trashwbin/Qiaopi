package com.qiaopi.service;

import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.Letter;
import com.qiaopi.dto.LetterSendDTO;
import com.qiaopi.vo.LetterVO;
import org.springframework.http.ResponseEntity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface LetterService {

//    /**
//     * 绘制逆时针旋转90度的文本
//     *
//     * @param g2d  Graphics2D 对象
//     * @param text 需要绘制的文本
//     * @param x    文本起始绘制位置的x坐标
//     * @param y    文本起始绘制位置的y坐标
//     */
//    void Main(Graphics2D g2d, String text, int x, int y);
//
//    /**
//     * 绘制逆时针旋转90度的文本，并设置背景图片、字体和颜色
//     *
//     * @param g2d        Graphics2D 对象
//     * @param text       需要绘制的文本
//     * @param width      图片宽度
//     * @param height     图片高度
//     * @param color      字体颜色
//     * @param font       字体大小
//     * @param stationery 信纸类型
//     * @param x          文本起始绘制位置的x坐标
//     * @param y          文本起始绘制位置的y坐标
//     */
//    void drawMain(Graphics2D g2d, String text, int width, int height, String color, String font, String stationery, int x, int y) throws IOException;
//
//    /**
//     * 绘制逆时针旋转90度的文本
//     *
//     * @param g2d Graphics2D 对象
//     * @param sender 需要绘制的文本
//     * @param x 文本起始绘制位置的x坐标
//     * @param y 文本起始绘制位置的y坐标
//     */
//    void Sender(Graphics2D g2d, String sender, int x, int y);
//
//    /**
//     * 绘制逆时针旋转90度的文本，并设置背景图片、字体和颜色
//     * @param g2d        Graphics2D 对象
//     * @param sender     需要绘制的文本
//     * @param width      图片宽度
//     * @param height     图片高度
//     * @param color      字体颜色
//     * @param font       字体大小
//     * @param stationery 信纸类型
//     * @param x          文本起始绘制位置的x坐标
//     * @param y          文本起始绘制位置的y坐标
//     */
//    void drawSender(Graphics2D g2d, String sender, int width, int height, String color, String font, String stationery, int x, int y) throws IOException;
//
//    /**
//     * 绘制逆时针旋转90度的文本
//     *
//     * @param g2d   Graphics2D 对象
//     * @param recipient 需要绘制的文本
//     * @param x     文本起始绘制位置的x坐标
//     * @param y     文本起始绘制位置的y坐标
//     */
//    void Recipient(Graphics2D g2d, String recipient, int x, int y);
//
//    /**
//     * 绘制逆时针旋转90度的文本，并设置背景图片、字体和颜色
//     *
//     * @param g2d        Graphics2D 对象
//     * @param recipient  需要绘制的文本
//     * @param width      图片宽度
//     * @param height     图片高度
//     * @param color      字体颜色
//     * @param font       字体大小
//     * @param stationery 信纸类型
//     * @param x          文本起始绘制位置的x坐标
//     * @param y          文本起始绘制位置的y坐标
//     */
//    void drawRecipient(Graphics2D g2d, String recipient, int width, int height, String color, String font, String stationery, int x, int y) throws IOException ;
//
//
//    /**
//     * 生成参数
//     * @param letterGenDTO
//     * @return
//     */
//    ArrayList<String> generationParameters(LetterGenDTO letterGenDTO);
//
//
//    /**
//     * 旋转图片
//     * @param originalImage
//     * @param degrees
//     * @return
//     */
//    BufferedImage rotateImage(BufferedImage originalImage, int degrees);
//
//
//    /**
//     * 生成字体照片
//     * @param letterGenDTO
//     * @return
//     */
    //String generateImage(LetterGenDTO letterGenDTO);

  //  String generateImage(LetterGenDTO letterGenDTO, Long currnetUserId);
//
//
//    /**
//     * 生成封面照片
//     *
//     * @param letterSendDTO
//     * @return
//     */
//    String coverGenerieren(LetterSendDTO letterSendDTO) throws IOException;
//
//
//    /**
//     * 绘制封面主要文本
//     * @param g2d
//     * @param text
//     * @param x
//     * @param y
//     */
//    void coverMain(Graphics2D g2d, String text, int x, int y);
//
//    /**
//     * 绘制封面主要文本，并设置背景图片、字体和颜色
//     *
//     * @param g2d
//     * @param text
//     * @param width
//     * @param height
//     * @param x
//     * @param y
//     * @throws IOException
//     */
//    void drawCoverMain(Graphics2D g2d, String text, int width, int height, int x, int y) throws IOException;
//
//    /**
//     * 绘制封面副文本
//     *
//     * @param g2d
//     * @param text
//     * @param x
//     * @param y
//     */
//    void coverSubordinate(Graphics2D g2d, String text, int x, int y);
//
//
//    /**
//     * 绘制封面副文本，并设置背景图片、字体和颜色
//     *
//     * @param g2d
//     * @param text
//     * @param width
//     * @param height
//     * @param x
//     * @param y
//     * @throws IOException
//     */
//    void drawCoverSubordinate(Graphics2D g2d, String text, int width, int height, int x, int y) throws IOException;


    /**
     *
     */
    void sendLetterToEmail(List<Letter> letters);

    /**
     * 发送信件前封装
     *
     * @param letterSendDTO
     * @return
     */
    LetterVO sendLetterPre(LetterSendDTO letterSendDTO);

    /**
     * 获取我写的侨批
     * @return
     */
    List<LetterVO> getMySendLetter();

    List<LetterVO> getMyReceiveLetter();

    LetterVO getMyNotReadLetter();

    void readLetter(Long letterId);
}





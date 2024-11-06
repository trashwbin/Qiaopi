package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.constant.FriendConstants;
import com.qiaopi.constant.LetterConstants;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.LetterSendDTO;
import com.qiaopi.entity.*;
import com.qiaopi.exception.letter.LetterException;
import com.qiaopi.exception.user.UserException;
import com.qiaopi.exception.user.UserNotExistsException;
import com.qiaopi.mapper.*;
import com.qiaopi.service.LetterService;
import com.qiaopi.utils.PositionUtil;
import com.qiaopi.utils.ProgressUtils;
import com.qiaopi.vo.LetterVO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.qiaopi.utils.MessageUtils.message;
import static com.qiaopi.constant.CacheConstant.*;


@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class LetterServiceImpl implements LetterService {
    private final FontColorMapper fontColorMapper;
    private final FontMapper fontMapper;
    private final PaperMapper paperMapper;
    private final FileStorageService fileStorageService;
    private final LetterMapper letterMapper;
    private final UserMapper userMapper;
    private final JavaMailSender javaMailSender;
    private final CountryMapper countryMapper;
    private final RedisTemplate redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    @Value("${spring.mail.username}")
    private String sender;
    @Value("${spring.mail.nickname}")
    private String nickname;

    // Cover
    private ExecutorService subExecutorService = Executors.newFixedThreadPool(3);
    public String coverGenerieren(LetterSendDTO letterSendDTO,Long userId) {
        // 设置图片的宽和高（根据实际需求可以动态调整）
        int width = 1000; // 图片宽度
        int height = 550; // 图片高度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像

        // TODO 如何正确截取地址和用户名字
        String tempMailingAddress = letterSendDTO.getSenderAddress().getFormattedAddress(); // 寄件人地址
        String mailingAddress = tempMailingAddress.substring(3, 6);

        String tempInsideAddress = letterSendDTO.getRecipientAddress().getFormattedAddress(); // 收件人地址
        String insideAddress = tempInsideAddress.substring(0, 6);

        String sender = letterSendDTO.getSenderName(); // 寄件人姓名
        String recipient = letterSendDTO.getRecipientName(); // 收件人姓名

        // 收信地址
        drawCoverMain(g2d, insideAddress, width, height, 156, 185);

        // 创建 Graphics2D 对象用于绘制子内容
        Graphics2D fontG2d = drawCoverSubordinate(g2d);
        // 创建并行任务
        CompletableFuture<Void> recipientFuture = CompletableFuture.runAsync(() -> {
                    Graphics2D clonedG2D = (Graphics2D) fontG2d.create();
                    coverSubordinate(clonedG2D, recipient, 155, 25);
                    clonedG2D.dispose();
                }, subExecutorService);
        CompletableFuture<Void> senderFuture = CompletableFuture.runAsync(() -> {
                    Graphics2D clonedG2D = (Graphics2D) fontG2d.create();
                    coverSubordinate(clonedG2D, sender, 750, 405);
                    clonedG2D.dispose();
                }, subExecutorService);
        CompletableFuture<Void> mailingAddressFuture = CompletableFuture.runAsync(() -> {
                    Graphics2D clonedG2D = (Graphics2D) fontG2d.create();
                    coverSubordinate(clonedG2D, mailingAddress, 440, 405);
                    clonedG2D.dispose();
                }, subExecutorService);

        // 等待所有任务完成
        CompletableFuture.allOf(recipientFuture, senderFuture, mailingAddressFuture).join();

        // 释放 Graphics2D 资源
        g2d.dispose();
        bufferedImage = rotateImage2(bufferedImage, 90);

        String url = null;
        byte[] imageBytes = null; // 获取字节数组

        // 耗时1秒
        try {
            // 将图片写入字节流
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); // 创建字节数组输出流
            Thumbnails.of(bufferedImage)
                    .outputFormat("png") // 使用 JPEG 格式
                    .outputQuality(0.8) // 设置压缩质量，0.8 表示 80% 的质量
                    .size(bufferedImage.getWidth(), bufferedImage.getHeight())
                    .toOutputStream(baos);
            imageBytes = baos.toByteArray();

            // 生成一个随机的文件名
            String fileName = "user-" + userId + "-" + UUID.randomUUID() + ".png";
            // 将照片存储到服务器
            FileInfo fileInfo = fileStorageService.of(imageBytes).setSaveFilename(fileName).setPath("cover/").upload();
            url = fileInfo.getUrl();

        } catch (IOException e) {
            log.error("生成封面照片失败", e);
        }
        // 这里实在没办法了,原图片有透明部分,转换成jpg会变成黑色,只能用png
        return url;
    }
    public void coverMain(Graphics2D g2d, String text, int x, int y) {
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

    // 使用 ConcurrentHashMap 作为线程安全的缓存
    private static final Map<String, BufferedImage> resourcesCache = new ConcurrentHashMap<>();
    private static final Map<String, Font> coverFontCache = new ConcurrentHashMap<>();

    public void drawCoverMain(Graphics2D g2d, String text, int width, int height, int x, int y) {
        // 加载书信图片
        String imagePath = "images/Cover/Cover.png";
        // 检查缓存中是否存在该图像
        BufferedImage bgImage = resourcesCache.get(imagePath);
        if (bgImage == null) {
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(imagePath);
                if (inputStream == null) {
                    log.error("无法找到指定的图像文件: " + imagePath);
                } else {
                    bgImage = ImageIO.read(inputStream);
                    // 将图像添加到缓存中
                    resourcesCache.put(imagePath, bgImage);
                }
            } catch (IOException e) {
                System.err.println("加载背景图片时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
        //背景图适配绘制
        g2d.drawImage(bgImage, 0, 0, width, height, null);

        // 调整字体文件路径以匹配类路径
        String fontPath = "fonts/CoverFont/1.TTF";
        // 检查缓存中是否存在该图像
        Font customFont = coverFontCache.get(fontPath + "160");
        if (customFont == null) {
            try {

                // 使用类加载器获取字体文件输入流
                InputStream fontStream = getClass().getClassLoader().getResourceAsStream(fontPath);
                if (fontStream != null) {
                    // 加载字体文件
                    customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont((float) 160);
                } else {
                    log.error("字体文件未找到: " + fontPath);
                }

                // 获取本地图形环境并注册字体
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);

            } catch (FontFormatException | IOException e) {
                // 如果字体加载失败，使用默认字体
                customFont = new Font("宋体", Font.PLAIN, 100); // 使用支持中文的默认字体，例如宋体
            }
        }

        // 设置字体及颜色
        g2d.setFont(customFont); // 设置字体
        g2d.setColor(Color.decode("#030303")); // 设置字体颜色


        coverMain(g2d, text, x, y);
    }
    public Graphics2D drawCoverSubordinate(Graphics2D g2d){

      // 调整字体文件路径以匹配类路径
      String fontPath = "fonts/CoverFont/1.TTF";
      // 检查缓存中是否存在该图像
      Font customFont = coverFontCache.get(fontPath+"110");
      if (customFont == null) {
          try {

              // 使用类加载器获取字体文件输入流
              InputStream fontStream = getClass().getClassLoader().getResourceAsStream(fontPath);
              if (fontStream != null) {
                  // 加载字体文件
                  customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont((float) 110);
              } else {
                  log.error("字体文件未找到: " + fontPath);
              }

              // 获取本地图形环境并注册字体
              GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
              ge.registerFont(customFont);

          } catch (FontFormatException | IOException e) {
              // 如果字体加载失败，使用默认字体
              customFont = new Font("宋体", Font.PLAIN, 100); // 使用支持中文的默认字体，例如宋体
          }
      }

        g2d.setFont(customFont); // 设置字体
        g2d.setColor(Color.decode("#030303")); // 设置字体颜色*/
        /*// 释放图形资源
        g2d.dispose(); // 释放Graphics2D对象*/
        return g2d;
    }

    // 封面的旋转函数!很重要的
    public BufferedImage rotateImage2(BufferedImage originalImage, int degrees) {
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

    public void coverSubordinate(Graphics2D g2d, String text, int x, int y) {
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

    public  String convertToHttpsAndRemovePort(String url) {
        // 使用正则表达式匹配 HTTP 协议和端口号
        String pattern = "^http://(.*?)(:\\d+)?(/.*)$";
        String replacement = "https://$1$3";
        // 替换匹配的部分
        return url.replaceAll(pattern, replacement);
    }

    public static String convertImageUrlToBase64(String imageUrl) throws IOException {
        // 从 URL 获取图片
        URL url = new URL(imageUrl);
        InputStream in = url.openStream();
        BufferedImage bufferedImage = ImageIO.read(in);
        in.close();

        // 将图片转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, getFormatNameFromUrl(imageUrl), baos);
        byte[] imageBytes = baos.toByteArray();
        baos.close();

        // 将字节数组转换为 Base64 编码的字符串
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        return base64Image;
    }

    private static String getFormatNameFromUrl(String imageUrl) {
        // 从 URL 中提取图片格式（如 png, jpg 等）
        int lastDotIndex = imageUrl.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("Invalid image URL format");
        }
        return imageUrl.substring(lastDotIndex + 1).toLowerCase();
    }
    @Override
    @Transactional
    public void sendLetterToEmail(List<Letter> letters) {
        // 创建一个邮件
        //SimpleMailMessage message = new SimpleMailMessage();
        // 创建一个 MimeMessage 代替 SimpleMailMessage
        MimeMessage message = javaMailSender.createMimeMessage();

        for (Letter letter : letters) {
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                // 设置发件人
                helper.setFrom(nickname + '<' + sender + '>');
                String coverLink = convertToHttpsAndRemovePort(letter.getCoverLink());
                String imageUrl = letter.getCoverLink();
                String base64Image = "";
                try {
                    base64Image = convertImageUrlToBase64(imageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("图片转换失败", e);
                }
                String coverBase64= "data:image/png;base64," + base64Image;
                // 设置收件人
                helper.setTo(letter.getRecipientEmail());

                // 设置邮件主题
                helper.setSubject("您的好友给您寄了一封侨批喔");

                // 定义邮件内容，使用 HTML,
                //(QQ邮箱网页版会屏蔽ip的图片)
                String content = "<!DOCTYPE html>\n" +
                        "<html lang=\"zh-CN\">\n" +
                        "\n" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "</head>\n" +
                        "\n" +
                        "<body>\n" +
                        "\n" +
                        "  <div id=\"mailContentContainer\" onclick=\"getTop().previewContentImage(event, '')\"\n" +
                        "    onmousemove=\"getTop().contentImgMouseOver(event, '')\" onmouseout=\"getTop().contentImgMouseOut(event, '')\"\n" +
                        "    class=\"box\" style=\"opacity: 1;\">\n" +
                        "    <style>\n" +
                        "      /* 邮件内部图片支持调起预览。 */\n" +
                        "      img[image-inside-content='1'] {\n" +
                        "        cursor: pointer;\n" +
                        "      }\n" +
                        "    </style>\n" +
                        "    <style type=\"text/css\">\n" +
                        "      .box #shiyanlou-content {\n" +
                        "        background: url(https://www.taoyuantudigong.org.tw/main/wp-content/themes/project-theme/src/img/yellow.png) 0 0 / 400px auto repeat, #f9f9f9;\n" +
                        "        width: 80%;\n" +
                        "        margin: 0 auto;\n" +
                        "        font-family: 'Microsoft Yahei';\n" +
                        "        font-size: 1em;\n" +
                        "        /* 基础字体大小 */\n" +
                        "        letter-spacing: 0.1em;\n" +
                        "        color: #333;\n" +
                        "        line-height: 1.5em;\n" +
                        "        /* 行高 */\n" +
                        "        border-radius: 1.25em;\n" +
                        "        /* 20px */\n" +
                        "        padding: 1.25em;\n" +
                        "        z-index: -5;\n" +
                        "      }\n" +
                        "\n" +
                        "      .box #header {\n" +
                        "        border-bottom: solid 1px #eee;\n" +
                        "      }\n" +
                        "\n" +
                        "      .box #header a {\n" +
                        "        background-size: cover;\n" +
                        "        background-repeat: no-repeat;\n" +
                        "        display: inline-block;\n" +
                        "        height: 2.75em;\n" +
                        "        /* 44px */\n" +
                        "        width: 8.4375em;\n" +
                        "        /* 135px */\n" +
                        "        background-image: url('https://s2.loli.net/2024/10/12/QUhlj7zZnLpqimS.png');\n" +
                        "      }\n" +
                        "\n" +
                        "      .box #header #icon {\n" +
                        "        background-size: cover;\n" +
                        "        background-repeat: no-repeat;\n" +
                        "        height: 2.75em;\n" +
                        "        /* 44px */\n" +
                        "        width: 2.75em;\n" +
                        "        /* 44px */\n" +
                        "        background-image: url('https://s2.loli.net/2024/11/04/ZclRIekqd8giXhQ.png');\n" +
                        "        margin-top: -70px;\n" +
                        "        margin-right: 25px;\n" +
                        "        float: right;\n" +
                        "      }\n" +
                        "\n" +
                        "      .box #body {\n" +
                        "        margin-top: 0.625em;\n" +
                        "        /* 10px */\n" +
                        "        background-image: url('https://s2.loli.net/2024/10/12/Zhp7vkmxq6uV8La.png');\n" +
                        "        border-radius: 1.25em;\n" +
                        "        /* 20px */\n" +
                        "        background-position: bottom;\n" +
                        "        background-size: cover;\n" +
                        "        background-repeat: no-repeat;\n" +
                        "        position: relative;\n" +
                        "      }\n" +
                        "\n" +
                        "      .box #body p img {\n" +
                        "        width: auto;\n" +
                        "      }\n" +
                        "\n" +
                        "      .box #footer {\n" +
                        "        padding: 0.5em 0;\n" +
                        "        /* 8px */\n" +
                        "        color: #999;\n" +
                        "        letter-spacing: 0.1em;\n" +
                        "        border-top: solid 1px #eee;\n" +
                        "      }\n" +
                        "\n" +
                        "      .message {\n" +
                        "        font-size: 1.125em;\n" +
                        "        /* 18px */\n" +
                        "        color: #333;\n" +
                        "        /* 100px */\n" +
                        "        display: block;\n" +
                        "      }\n" +
                        "\n" +
                        "      .message a {\n" +
                        "        color: #E1B3FF;\n" +
                        "        text-decoration: none;\n" +
                        "        border-bottom: 0.0625em solid #007bff;\n" +
                        "        /* 1px */\n" +
                        "        transition: border-bottom 0.3s ease;\n" +
                        "      }\n" +
                        "\n" +
                        "      .message a:hover {\n" +
                        "        border-bottom: 0.125em solid #007bff;\n" +
                        "        /* 2px */\n" +
                        "      }\n" +
                        "\n" +
                        "      #cover {\n" +
                        "        background-size: cover;\n" +
                        "        background-repeat: no-repeat;\n" +
                        "        background-position: center;\n" +
                        "        display: block;\n" +
                        "        margin: 0 auto;\n" +
                        "        background-image: url("+coverLink+");\n" +
                        "        width: 12.5em;\n" +
                        "        /* 200px */\n" +
                        "        height: 21.0625em;\n" +
                        "        border-radius: 1.5em;\n" +
                        "        /* 335px */\n" +
                        "        z-index: 1;\n" +
                        "      }\n" +
                        "\n" +
                        "      #coverdatabase {\n" +
                        "        background-size: cover;\n" +
                        "        background-repeat: no-repeat;\n" +
                        "        background-position: center;\n" +
                        "        display: block;\n" +
                        "        margin: 0 auto;\n" +
                        "        background-image: url("+coverBase64+");\n" +
                        "        width: 12.5em;\n" +
                        "        /* 200px */\n" +
                        "        height: 21.0625em;\n" +
                        "        border-radius: 1.5em;\n" +
                        "        /* 335px */\n" +
                        "        z-index: 1;\n" +
                        "      }\n" +
                        "\n" +
                        "      #cover::before {\n" +
                        "        content: \"点击查看详情\";\n" +
                        "        position: absolute;\n" +
                        "        bottom: 60%;\n" +
                        "        right: 50%;\n" +
                        "        transform: translate(50%, 50%);\n" +
                        "        text-align: center;\n" +
                        "        padding: 0.625em 1.25em;\n" +
                        "        /* 10px 20px */\n" +
                        "        background-color: #007bff;\n" +
                        "        color: white;\n" +
                        "        border: 0.0625em solid #007bff;\n" +
                        "        /* 1px */\n" +
                        "        border-radius: 0.3125em;\n" +
                        "        /* 5px */\n" +
                        "        font-size: .7em;\n" +
                        "        /* 16px */\n" +
                        "        z-index: -1;\n" +
                        "      }\n" +
                        "    </style>\n" +
                        "    <div id=\"shiyanlou-content\">\n" +
                        "      <div id=\"header\">\n" +
                        "        <p style=\"display: flex; align-items: center;\">\n" +
                        "          <a href=\"http://110.41.58.26\" target=\"_blank\" rel=\"noopener\"></a>\n" +
                        "          <!-- <span style=\"color:#A52328; display:inline-block; line-height: 2.75em;\">侨缘信使</span> -->\n" +
                        "        </p>\n" +
                        "        <div id=\"icon\"></div>\n" +
                        "      </div>\n" +
                        "      <p class=\"message\">"+letter.getRecipientName()+",您的好友给您发了一封侨批喔，<a href=\"http://110.41.58.26\">快来看看吧</a></p>\n" +
                        "      <div id=\"body\">\n" +
                        "\n" +
                        "        <p>&nbsp;</p>\n" +
                        "        <a href=\"http://110.41.58.26\" style=\"margin: 0 auto; display: block; height: 21.0625em; \">\n" +
                        "          <div id=\"cover\">\n" +
                        "            <div id=\"coverdatabase\">\n" +
                        "            </div>\n" +
                        "          </div>\n" +
                        "        </a>\n" +
                        "        <p class=\"margin10\">&nbsp;</p>\n" +
                        "      </div>\n" +
                        "      <div id=\"footer\">\n" +
                        "        <p>再次感谢您对<span style=\"color:#A52328\">侨缘信使</span>的支持！</p>\n" +
                        "        <p><span style=\"color:#A52328\">侨缘信使团队</span>，敬上</p>\n" +
                        "      </div>\n" +
                        "    </div>\n" +
                        "  </div>\n" +
                        "</body>\n" +
                        "\n" +
                        "</html>";

                // 设置邮件内容为 HTML
                helper.setText(content, true);
                // 发送邮件
                javaMailSender.send(message);
            } catch (MessagingException e) {
                log.error(message("letter.sent.failed"), letter);
                ;
            } catch (MailException e) {
                log.error(message("letter.sent.failed.by.email"), letter);
                ;
            } catch (Exception e) {
                log.error(message("unknown.error"), letter);
                ;
            }
            letter.setStatus(LetterConstants.DELIVERED);
            letter.setDeliveryProgress(10000L);
            letter.setDeliveryTime(LocalDateTime.now());
            letter.setUpdateUser(-1L);
            letterMapper.updateById(letter);
        }
    }

    // 主线程池
    private ExecutorService mainExecutorService = Executors.newFixedThreadPool(3);

    @Override
    @Transactional
    public LetterVO sendLetterPre(LetterSendDTO letterSendDTO) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }

        Letter letter = BeanUtil.copyProperties(letterSendDTO, Letter.class);

        // 检验地址是否有效
        if(letter.getRecipientAddress().getCountryId()!=(long)1){
            Country country = countryMapper.selectById(letter.getRecipientAddress().getCountryId());
            if(country==null){
                throw new UserException(message("user.address.country.not.exists"));
            }
            letter.getRecipientAddress().setLatitude(country.getCapitalLatitude());
            letter.getRecipientAddress().setLongitude(country.getCapitalLongitude());
        }
        if(letter.getSenderAddress().getCountryId()!=(long)1){
            Country country = countryMapper.selectById(letter.getSenderAddress().getCountryId());
            if(country==null){
                throw new UserException(message("user.address.country.not.exists"));
            }
            letter.getSenderAddress().setLatitude(country.getCapitalLatitude());
            letter.getSenderAddress().setLongitude(country.getCapitalLongitude());
        }

        // 检验猪仔钱是否足够
        if (user.getMoney() >= letterSendDTO.getPiggyMoney()) {
            if (letterSendDTO.getPiggyMoney()< 0) {
                throw new UserException(message("user.money.not.match"));
            }
            user.setMoney(user.getMoney() - letterSendDTO.getPiggyMoney());
            // 顺带做个地址处理你再更新
            List<Address> addresses = user.getAddresses();
            Address senderAddress = letter.getSenderAddress();

            boolean isInAddresses = false;
            if (CollUtil.isEmpty(addresses)) {
                addresses = new ArrayList<>();
                senderAddress.setIsDefault(String.valueOf(true));
                senderAddress.setId(1L);
            }else {
                for (Address address : addresses) {
                    if (address.getFormattedAddress().equals(senderAddress.getFormattedAddress())||address.getId().equals(senderAddress.getId())) {
                        isInAddresses = true;
                        senderAddress.setId(address.getId());
                        break;
                    }
                }
            }
            if (!isInAddresses) {
                if (senderAddress.getId() == null&& !addresses.isEmpty()) {
                senderAddress.setId(addresses.get(addresses.size()-1).getId() + 1L);
                }
                addresses.add(senderAddress);
                letter.setSenderAddress(senderAddress);
                user.setAddresses(addresses);
                stringRedisTemplate.delete(CACHE_USER_ADDRESSES_KEY + userId);
            }
            userMapper.updateById(user);
        } else {
            throw new UserException(message("user.money.not.enough"));
        }

        //检查是否是好友
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>().eq(Friend::getUserId, letter.getRecipientUserId()).eq(Friend::getOwningId, userId));
        if (friend == null) {
            letter.setRemark("new friend");
        }else{
            // 顺带做个地址处理你再更新
            List<Address> addresses = friend.getAddresses();
            Address friendAddress = letter.getRecipientAddress();

            boolean isInAddresses = false;
            if (CollUtil.isEmpty(addresses)) {
                addresses = new ArrayList<>();
                friendAddress.setIsDefault(String.valueOf(true));
                friendAddress.setId(1L);
            }else {
                for (Address address : addresses) {
                    if (address.getFormattedAddress().equals(friendAddress.getFormattedAddress())||address.getId().equals(friendAddress.getId())) {
                        isInAddresses = true;
                        friendAddress.setId(address.getId());
                        break;
                    }
                }
            }
            if (!isInAddresses) {
                if (friendAddress.getId() == null&& !addresses.isEmpty()) {
                    friendAddress.setId(addresses.get(addresses.size() - 1).getId() + 1L);
                }
                addresses.add(friendAddress);
                letter.setRecipientAddress(friendAddress);
                friend.setAddresses(addresses);
                // 先这样吧后续再优化
                stringRedisTemplate.delete(CACHE_USER_FRIENDS_KEY + userId);
                friendMapper.updateById(friend);
            }
        }
        // 创建任务
        CompletableFuture<String> coverFuture = CompletableFuture.supplyAsync(() -> {
            return coverGenerieren(letterSendDTO,userId);
        }, mainExecutorService);

        CompletableFuture<String> letterLinkFuture = CompletableFuture.supplyAsync(() -> {
            String fileName = "user-" + userId + "-" + UUID.randomUUID() + ".jpg";
            String redisKey = "image:" + userId;
            String base64Image = (String) redisTemplate.opsForValue().get(redisKey);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            assert imageBytes != null;
            FileInfo fileInfo = fileStorageService.of(imageBytes).setSaveFilename(fileName).setPath("letter/").upload();
            return fileInfo.getUrl();
        }, mainExecutorService);

        CompletableFuture<LocalDateTime> deliveryTimeFuture = CompletableFuture.supplyAsync(() -> {

            double distance = PositionUtil.getDistance(
                    letter.getSenderAddress().getLongitude(),
                    letter.getSenderAddress().getLatitude(),
                    letter.getRecipientAddress().getLongitude(),
                    letter.getRecipientAddress().getLatitude()
            );
            distance = Math.max(distance, 200000); // 200公里转换为米
            double time = distance / 40000;
            long timeMin = (long) (time * 60 * 60);
            LocalDateTime now = LocalDateTime.now();
            return now.plusSeconds(timeMin);
        }, mainExecutorService);
        String coverLink = null;
        String letterLink = null;
        LocalDateTime deliveryTime = null;

        try {
            // 等待所有任务完成
            coverLink = coverFuture.get();
            letterLink = letterLinkFuture.get();
            deliveryTime = deliveryTimeFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw  new LetterException(message("letter.send.failed"));
        }


        // 设置信件属性
        letter.setCoverLink(coverLink);
        letter.setLetterLink(letterLink);
        letter.setExpectedDeliveryTime(deliveryTime);
        letter.setDeliveryTime(deliveryTime);
        letter.setStatus(LetterConstants.TRANSIT);
        letter.setReadStatus(LetterConstants.NOT_READ);
        letter.setDeliveryProgress(0L);
        letter.setSenderEmail(user.getEmail());
        letter.setSpeedRate("1");
        letter.setSenderUserId(userId);
        letter.setLetterContent(letterSendDTO.getLetterContent().trim());
        letter.setRecipientEmail(letterSendDTO.getRecipientEmail().toLowerCase());
        letter.setPiggyMoney(letterSendDTO.getPiggyMoney());

        letterMapper.insert(letter);
        stringRedisTemplate.delete(CACHE_USER_WRITE_LETTER_KEY + userId);

        return BeanUtil.copyProperties(letter, LetterVO.class);
    }
    @Override
    public List<LetterVO> getMySendLetter() {

        Long userId = UserContext.getUserId();
        List<Letter> letters = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_USER_WRITE_LETTER_KEY + userId), Letter.class);

        if (CollUtil.isEmpty(letters)) {
            letters=letterMapper.selectList(new LambdaQueryWrapper<Letter>().eq(Letter::getSenderUserId, userId).orderByDesc(Letter::getCreateTime));
        }
        //每次要查的时候再更新这个数据，减少更新次数
        // 只有status是2的才要更新
        letters.replaceAll(letter -> letter.getStatus() == 2 ? ProgressUtils.getProgress(letter) : letter);
        stringRedisTemplate.opsForValue().set(CACHE_USER_WRITE_LETTER_KEY + userId, JSONUtil.toJsonStr(letters), Duration.ofHours(12));

        //更新进度
        letterMapper.updateById(letters);
        return BeanUtil.copyToList(letters, LetterVO.class);
    }

    @Override
    public List<LetterVO> getMyReceiveLetter() {
        User user = userMapper.selectById(UserContext.getUserId());
        Letter hello = letterMapper.selectById(1);
        //查询收信人为当前用户的信件
        List<Letter> letters = letterMapper.selectList(new LambdaQueryWrapper<Letter>().eq(Letter::getRecipientEmail, user.getEmail()).eq(Letter::getStatus, LetterConstants.DELIVERED).orderByDesc(Letter::getExpectedDeliveryTime));
        letters.forEach(letter -> {
            letter.setRecipientUserId(UserContext.getUserId());
        });
        //更新letter的收信人id
        letterMapper.updateById(letters);
        letters.add(hello);
        return BeanUtil.copyToList(letters, LetterVO.class);
    }

    @Override
    public LetterVO getMyNotReadLetter() {
        //获取这人的全部收到的信
        List<LetterVO> myReceiveLetter = getMyReceiveLetter();
        if (myReceiveLetter.isEmpty()) {
            return null;
        }
        //返回第一封信,如果已读,后面的也不弹窗
        LetterVO letterVO = myReceiveLetter.get(0);
        if (letterVO.getReadStatus() == LetterConstants.READ) {
            return null;
        }
        //筛选未读的信
        return letterVO;
    }

    @Override
    public void readLetter(Long letterId) {
        Letter letter = letterMapper.selectById(letterId);
        if (letter == null) {
            throw new LetterException(message("letter.not.exists"));
        }
        if (letter.getReadStatus() == LetterConstants.READ) {
//            throw new LetterException(message("letter.already.read"));
            return;
        }
        if (!UserContext.getUserId().equals(letter.getRecipientUserId())) {
            throw new LetterException(message("letter.not.yours"));
        }
        // 如果是新朋友的信,就加好友
        if (letter.getRemark()!=null&&letter.getRemark().contains("new friend")) {
            FriendRequest friendRequest = FriendRequest.builder()
                    .receiverId(UserContext.getUserId())
                    .senderId(letter.getSenderUserId())
                    .status(FriendConstants.PENDING)
                    .giveAddress(letter.getSenderAddress())
                    .content(letter.getRecipientName() + "!我给你写了一封侨批哦,快来加我为好友吧!")
                    .bottleId(letterId)
                    .build();
            friendRequest.setCreateTime(letter.getDeliveryTime());
            friendRequestMapper.insert(friendRequest);
        }
        // 读信后加钱
        if (letter.getPiggyMoney()>0){
            User user = userMapper.selectById(UserContext.getUserId());
            user.setMoney(user.getMoney()+letter.getPiggyMoney());
            userMapper.updateById(user);
        }
        letter.setReadStatus(LetterConstants.READ);
        letterMapper.updateById(letter);
    }


}




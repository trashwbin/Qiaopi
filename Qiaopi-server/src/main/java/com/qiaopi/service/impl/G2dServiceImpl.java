package com.qiaopi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiaopi.constant.LetterConstants;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.FontPaper;
import com.qiaopi.entity.Paper;
import com.qiaopi.mapper.FontColorMapper;
import com.qiaopi.mapper.FontMapper;
import com.qiaopi.mapper.FontPaperMapper;
import com.qiaopi.mapper.PaperMapper;
import com.qiaopi.service.G2dService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class G2dServiceImpl implements G2dService {

    private final FontColorMapper fontColorMapper;

    private final FontMapper fontMapper;

    private final PaperMapper paperMapper;

    private final FontPaperMapper paperFontMapper;

    private final RedisTemplate redisTemplate;


    // 缓存字体
    private final Map<String, Font> fontCache = new HashMap<>();

    // 缓存字体
    private final Map<String, Font> fontCache2 = new HashMap<>();

    // 缓存背景图片
    private final Map<String, BufferedImage> bgImageCache = new HashMap<>();

    @Override
    public String generateImage(LetterGenDTO letterGenDTO, Long currentUserId) {


        // 遍历输入的文本内容
        String contentToCheck = letterGenDTO.getLetterContent();
        // 直接检查整个字符串是否只包含中文字符、标点符号或空白字符
        boolean containsOnlyChineseOrSymbols = contentToCheck.matches("^[\\u4e00-\\u9fa5\\p{Punct}\\s]*$");


        BufferedImage bufferedImage;
        // 根据布尔值的结果做出相应操作
        if (!containsOnlyChineseOrSymbols) {
            log.info("文本中包含字母或数字，进行处理。");
            bufferedImage = createAndDrawImage2(letterGenDTO);
        } else {
            log.info("文本仅包含汉字或符号，进行处理。");
            bufferedImage = createAndDrawImage(letterGenDTO);
        }


        //存储
        try {
            // 将图片写入字节流
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); // 创建字节数组输出流
            ImageIO.write(bufferedImage, "png", baos); // 将BufferedImage写入字节数组输出流
            byte[] imageBytes = baos.toByteArray(); // 获取字节数组

            // 将字节数组转换为Base64编码的字符串
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // 生成 Redis 中存储的 key
            String redisKey = "image:" + currentUserId; // 假设有用户 ID 或其他标识符

            // 将 Base64 字符串存入 Redis
            redisTemplate.opsForValue().set(redisKey, base64Image);


          /*  // 生成一个随机的文件名
            String fileName =  UUID.randomUUID()+ ".png";
            //将照片存储到服务器
            FileInfo fileInfo = fileStorageService.of(imageBytes).setSaveFilename(fileName).setPath("letter/").upload();
            url = fileInfo.getUrl();
            */

           /* // 设置响应头并返回图片
            HttpHeaders headers = new HttpHeaders(); // 创建HttpHeaders对象
            headers.setContentType(MediaType.IMAGE_PNG); // 设置响应内容类型为PNG图片
            headers.setContentLength(imageBytes.length); // 设置响应内容长度
            //return ResponseEntity.ok().headers(headers).body(imageBytes); // 返回包含图片字节数组的响应实体
*/
            return base64Image;

        } catch (IOException e) {
            log.error("生成图片失败", e);
        }
        return null;


    }

    /**
     * 绘制出现非中文的信纸
     *
     * @param letterGenDTO
     * @return
     */
    private BufferedImage createAndDrawImage2(LetterGenDTO letterGenDTO) {
        //设置照片的宽和高
        int width = LetterConstants.NOT_ONLY_CHINESES_WIDTH;//照片宽度
        int height = LetterConstants.NOT_ONLY_CHINESES_HEIGHT;//照片宽度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //绘制书信样式
        //初始化 g2d  进行字体颜色，种类，背景图片等的绘制
        Graphics2D G2D = drawWritingStyle2(bufferedImage, letterGenDTO);

        //绘制字体样式
        drawFontStyle2(G2D, letterGenDTO);

        return bufferedImage;

    }

    private void drawFontStyle2(Graphics2D g2D, LetterGenDTO letterGenDTO) {

        //获取纸张信息，包括偏移量X，Y，适配字数
        Paper paper = paperMapper.selectById(letterGenDTO.getPaperId());
        Long fontId = letterGenDTO.getFontId();
        Long paperId = letterGenDTO.getPaperId();
        // 构建查询条件
        QueryWrapper<FontPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("font_id", fontId).eq("paper_id", paperId);
        // 查询 fontPaper 记录
        FontPaper fontPaper = paperFontMapper.selectOne(queryWrapper);
        int fitNumber = Math.toIntExact(fontPaper.getFitNumber());

        ExecutorService executor = Executors.newFixedThreadPool(3);
        // 提交任务 使用多线程进行（分别对内容，发送者名称，收件者名称进行旋转，转换成古代书法规则）
        executor.submit(() -> {
            Graphics2D clonedG2D = (Graphics2D) g2D.create();
            drawFontStyleDetail2(clonedG2D, letterGenDTO.getLetterContent(), Integer.parseInt(paper.getTranslateX()), Integer.parseInt(paper.getTranslateY()),fitNumber);
            //drawFontStyleDetail2(clonedG2D, letterGenDTO.getLetterContent(), 40, 70, Math.toIntExact(fitNumber));
            clonedG2D.dispose();
        });
        executor.submit(() -> {
            Graphics2D clonedG2D = (Graphics2D) g2D.create();
            //drawFontStyleDetail2(clonedG2D, letterGenDTO.getSenderName(), 350, 580, Math.toIntExact(fitNumber));
            drawFontStyleDetail2(clonedG2D, letterGenDTO.getSenderName(), Integer.parseInt(paper.getSenderTranslateX()), Integer.parseInt(paper.getSenderTranslateY()),fitNumber);
            clonedG2D.dispose();
        });
        executor.submit(() -> {
            Graphics2D clonedG2D = (Graphics2D) g2D.create();
            //drawFontStyleDetail2(clonedG2D, letterGenDTO.getRecipientName(), 30, 50, Math.toIntExact(fitNumber));
            drawFontStyleDetail2(clonedG2D, letterGenDTO.getRecipientName(), Integer.parseInt(paper.getRecipientTranslateX()), Integer.parseInt(paper.getRecipientTranslateY()),fitNumber);
            clonedG2D.dispose();
        });

        // 关闭线程池
        executor.shutdown();
        try {
            // 等待所有任务完成
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void drawFontStyleDetail2(Graphics2D g2d, String text, int x, int y, int fitNumber) {

     /*   int maxWidth = 380;

        // 初始化 x 和 y 坐标
        int currentX = x;
        int currentY = y;

        int spacing = 2; // 设置字符间距，适当增加以防重叠
        double letterLeftMargin = 6; // 字母左边距调整

        FontMetrics fontMetrics = g2d.getFontMetrics();
        int limitNumber = 0;
        int currentLineWidth = 0; // 当前行的宽度

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 计算当前字符的宽度
            int charWidth = fontMetrics.charWidth(c);

            // 检查是否需要换行
            if (currentLineWidth + charWidth + spacing > maxWidth) {
                currentX = x; // 重置 x 坐标
                currentY += fontMetrics.getHeight(); // 更新 y 坐标换行
                currentLineWidth = 0; // 重置当前行的宽度
            }

            // 根据字符类型调整偏移量
            int adjustedX = currentX;
            int adjustedY = currentY;

            // 如果是英文字母或数字，进行特殊偏移
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                adjustedX += letterLeftMargin; // 添加左边距
                // 绘制字符
                g2d.drawString(String.valueOf(c), adjustedX, adjustedY);
                // 更新 x 坐标，加入字符间距
                currentX += charWidth + 12;
            } else {
                // 绘制字符
                g2d.drawString(String.valueOf(c), adjustedX, adjustedY);
                // 更新 x 坐标，加入字符间距
                currentX += charWidth + spacing;
            }

            // 更新当前行的宽度
            currentLineWidth += charWidth + spacing;

            // 检查字符绘制限制
            limitNumber++;
            if (limitNumber == fitNumber) {
                break;
            }
        }*/
        // 文本换行和绘制
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int lineHeight = fontMetrics.getHeight();
        List<String> wrappedLines = wrapText(text, fontMetrics, 380 );

        int currentY = y;
        for (String line : wrappedLines) {
            g2d.drawString(line, x, currentY + fontMetrics.getAscent());
            currentY += lineHeight;
        }
    }
    private static List<String> wrapText(String text, FontMetrics fontMetrics, int maxWidth) {
       /* List<String> wrappedLines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String testLine = currentLine.toString() + c;
            int testWidth = fontMetrics.stringWidth(testLine);

            if (testWidth > maxWidth) {
                if (currentLine.length() > 0) {
                    wrappedLines.add(currentLine.toString());
                    currentLine = new StringBuilder(String.valueOf(c));
                } else {
                    wrappedLines.addAll(wrapLongWord(String.valueOf(c), fontMetrics, maxWidth));
                }
            } else {
                currentLine.append(c);
            }
        }

        if (currentLine.length() > 0) {
            wrappedLines.add(currentLine.toString());
        }

        return wrappedLines;*/
        List<String> wrappedLines = new ArrayList<>();

        // 按回车符拆分为多行
        String[] lines = text.split("\n");

        // 对每行分别进行换行处理
        for (String line : lines) {
            List<String> wrappedSubLines = wrapSingleLine(line, fontMetrics, maxWidth);
            wrappedLines.addAll(wrappedSubLines); // 添加换行后的行
        }

        return wrappedLines;
    }
    private static List<String> wrapSingleLine(String line, FontMetrics fontMetrics, int maxWidth) {
        List<String> wrappedLines = new ArrayList<>();
        String[] words = line.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            int testWidth = fontMetrics.stringWidth(testLine);

            if (testWidth > maxWidth) {
                if (currentLine.length() > 0) {
                    wrappedLines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    wrappedLines.addAll(wrapLongWord(word, fontMetrics, maxWidth));
                }
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ").append(word);
                } else {
                    currentLine.append(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            wrappedLines.add(currentLine.toString());
        }

        return wrappedLines;
    }
    private static List<String> wrapLongWord(String word, FontMetrics fontMetrics, int maxWidth) {
        List<String> parts = new ArrayList<>();
        while (fontMetrics.stringWidth(word) > maxWidth) {
            int breakPoint = findBreakPoint(word, fontMetrics, maxWidth);
            if (breakPoint == -1) {
                break;
            }
            parts.add(word.substring(0, breakPoint));
            word = word.substring(breakPoint);
        }
        parts.add(word);
        return parts;
    }

    private static int findBreakPoint(String word, FontMetrics fontMetrics, int maxWidth) {
        for (int i = 1; i < word.length(); i++) {
            if (fontMetrics.stringWidth(word.substring(0, i)) > maxWidth) {
                return i - 1;
            }
        }
        return -1;
    }


    private Graphics2D drawWritingStyle2(BufferedImage bufferedImage, LetterGenDTO letterGenDTO) {
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        //获取用户选定的字体颜色，字体样式，纸张
        //获取字体颜色，字体 纸张
        String fontColor = (fontColorMapper.selectById(letterGenDTO.getFontColorId())).getHexCode();

        String font = (fontMapper.selectById(letterGenDTO.getFontId())).getFilePath();

        String paper = "照片1.png";//TODO 这里需要去修改 查库获取

        // 从缓存中获取背景图片
        BufferedImage bgImage = bgImageCache.get(paper);
        if (bgImage == null) {
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("images/Stationery/" + paper);
                if (inputStream == null) {
                    log.error("无法找到指定的图像文件: images/Stationery/{}", paper);
                } else {
                    bgImage = ImageIO.read(inputStream);
                    bgImageCache.put(paper, bgImage); // 缓存背景图片
                }
            } catch (IOException e) {
                log.error("加载背景图片时发生错误: {}", e.getMessage());
            }
        }

        // 背景图适配绘制
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, width, height, null);
        } else {
            log.error("背景图片未找到");
        }
        // 加载自定义字体
        // 从缓存中获取字体


        Font customFont = fontCache2.get(font);

        if (customFont == null) {
            try {
                String fontPath = "fonts/MainContent/" + font;
                InputStream fontStream = getClass().getClassLoader().getResourceAsStream(fontPath);
                if (fontStream != null) {
                    customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont((float) 20);
                    fontCache2.put(font, customFont); // 缓存字体
                } else {
                    log.error("字体文件未找到: {}", fontPath);
                }

                // 获取本地图形环境并注册字体
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
            } catch (FontFormatException | IOException e) {
                // 如果字体加载失败，使用默认字体
                customFont = new Font("宋体", Font.PLAIN, 15); // 使用支持中文的默认字体，例如宋体
                log.error("加载字体文件时发生错误: {}", e.getMessage());
            }
        }
        // 设置字体及颜色
        g2d.setFont(customFont); // 设置字体
        g2d.setColor(Color.decode(fontColor)); // 设置字体颜色

        return g2d;
    }


    /**
     * 此方法用户绘制信件，包括信件和字体的样式
     *
     * @param letterGenDTO
     * @return
     */
    private BufferedImage createAndDrawImage(LetterGenDTO letterGenDTO) {

        //设置照片的宽和高
        int width = LetterConstants.ONLY_CHINESES_HEIGHT;//照片宽度
        int height = LetterConstants.ONLY_CHINESES_WIDTH;//照片宽度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);


        //绘制书信样式
        //初始化 g2d  进行字体颜色，种类，背景图片等的绘制
        Graphics2D G2D = drawWritingStyle(bufferedImage, letterGenDTO);

        //旋转字体
        drawFontStyle(G2D, letterGenDTO);

        //旋转文本
        bufferedImage = drawRotateImage(bufferedImage, 90);

        return bufferedImage;
    }

    /**
     * 旋转文本角度
     *
     * @param image
     * @param angle
     * @return
     */
    private BufferedImage drawRotateImage(BufferedImage image, int angle) {
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int w = image.getWidth();
        int h = image.getHeight();
        int newW = (int) Math.round(w * cos + h * sin);
        int newH = (int) Math.round(h * cos + w * sin);

        BufferedImage rotatedImage = new BufferedImage(newW, newH, image.getType());
        Graphics2D g2d = rotatedImage.createGraphics();
        g2d.translate((newW - w) / 2, (newH - h) / 2);
        g2d.rotate(radians, w / 2, h / 2);
        g2d.drawRenderedImage(image, null);
        g2d.dispose();

        return rotatedImage;
    }

    /**
     * 绘制书信内文本旋转
     *
     * @param g2D
     * @param letterGenDTO
     */
    private void drawFontStyle(Graphics2D g2D, LetterGenDTO letterGenDTO) {

        //获取纸张信息，包括偏移量X，Y，适配字数
        Paper paper = paperMapper.selectById(letterGenDTO.getPaperId());

        Long fontId = letterGenDTO.getFontId();
        Long paperId = letterGenDTO.getPaperId();
        //构建查询条件
        QueryWrapper<FontPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("font_id", fontId).eq("paper_id", paperId);
        //查询fontPaper记录
        FontPaper fontPaper = paperFontMapper.selectOne(queryWrapper);
        int fitNumber = Math.toIntExact(fontPaper.getFitNumber());

        ExecutorService executor = Executors.newFixedThreadPool(3);
        // 提交任务 使用多线程进行（分别对内容，发送者名称，收件者名称进行旋转，转换成古代书法规则）
        executor.submit(() -> {
            Graphics2D clonedG2D = (Graphics2D) g2D.create();
            drawFontStyleDetail(clonedG2D, letterGenDTO.getLetterContent(), Integer.parseInt(paper.getTranslateX()), Integer.parseInt(paper.getTranslateY()), fitNumber);
            clonedG2D.dispose();
        });
        executor.submit(() -> {
            Graphics2D clonedG2D = (Graphics2D) g2D.create();
            drawSenderFontStyleDetail(clonedG2D, letterGenDTO.getSenderName(), Integer.parseInt(paper.getSenderTranslateX()), Integer.parseInt(paper.getSenderTranslateY()), fitNumber);
            clonedG2D.dispose();
        });
        executor.submit(() -> {
            Graphics2D clonedG2D = (Graphics2D) g2D.create();
            drawFontStyleDetail(clonedG2D, letterGenDTO.getRecipientName(), Integer.parseInt(paper.getRecipientTranslateX()), Integer.parseInt(paper.getRecipientTranslateY()), fitNumber);
            clonedG2D.dispose();
        });

        // 关闭线程池
        executor.shutdown();
        try {
            // 等待所有任务完成
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    private void drawSenderFontStyleDetail(Graphics2D g2d, String text, int x, int y, int fitNumber) {
        //分析传进来的文本有多少个字符
        int textNumber = text.length();
        int temp = textNumber - 3;

        // 每行字符数，设置为15
        int charsPerLine = 15;
        // 当前绘制字符的 x 坐标，初始化为传入的 x 参数  可以根据字数自动配置位置
        //x变大，对应的字体下移
        int currentX = x - 50 * temp;
        // 当前绘制字符的 y 坐标，初始化为传入的 y 参数
        int currentY = y;

        int spacing = 30; // 设置字符间距
        double letterLeftMargin = 0; // Adjust this to control the left margin for letters


        FontMetrics fontMetrics = g2d.getFontMetrics();
        int LimitNumber = 0;
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

            // 根据字符类型调整 y 偏移量
            // Adjust y and x offsets based on character type
            int adjustedY = currentY;
            int adjustedX = currentX;

            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {

                //adjustedY += fontMetrics.charWidth(c) -0.5; // 基于字符宽度调整 y 偏移量
                adjustedY += fontMetrics.getAscent() / 2; // Fine-tune y offset for letters and digits
                adjustedX += letterLeftMargin; // Add left margin

                g2d.drawString(String.valueOf(c), currentX, adjustedY);
                // 增加字符间距
                currentX += fontMetrics.charWidth(c) + spacing;
            } else {
                //g2d.drawString(String.valueOf(c), currentX, currentY);
                g2d.drawString(String.valueOf(c), currentX, adjustedY);
                // 更新 x 坐标以便绘制下一个字符
                currentX += fontMetrics.charWidth(c);

            }

            // 检查是否需要换行
            if ((i + 1) % charsPerLine == 0 && i < text.length() - 1) {
                // 重置 x 坐标
                currentX = x;

                // 更新 y 坐标
                currentY += fontMetrics.getHeight();
            }
            LimitNumber++;
            if (LimitNumber == fitNumber) {
                break;
            }

        }


    }

    /**
     * 绘制书信内文本旋转细节
     *
     * @param g2d
     * @param text
     * @param x
     * @param y
     * @param fitNumber
     */
    public void drawFontStyleDetail(Graphics2D g2d, String text, int x, int y, int fitNumber) {

        // 每行字符数，设置为15
        int charsPerLine = 15;
        // 当前绘制字符的 x 坐标，初始化为传入的 x 参数
        int currentX = x;
        // 当前绘制字符的 y 坐标，初始化为传入的 y 参数
        int currentY = y;

        int spacing = 30; // 设置字符间距
        double letterLeftMargin = 0; // Adjust this to control the left margin for letters


        FontMetrics fontMetrics = g2d.getFontMetrics();
        int LimitNumber = 0;
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

            // 根据字符类型调整 y 偏移量
            // Adjust y and x offsets based on character type
            int adjustedY = currentY;
            int adjustedX = currentX;

            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {

                //adjustedY += fontMetrics.charWidth(c) -0.5; // 基于字符宽度调整 y 偏移量
                adjustedY += fontMetrics.getAscent() / 2; // Fine-tune y offset for letters and digits
                adjustedX += letterLeftMargin; // Add left margin

                g2d.drawString(String.valueOf(c), currentX, adjustedY);
                // 增加字符间距
                currentX += fontMetrics.charWidth(c) + spacing;
            } else {
                //g2d.drawString(String.valueOf(c), currentX, currentY);
                g2d.drawString(String.valueOf(c), currentX, adjustedY);
                // 更新 x 坐标以便绘制下一个字符
                currentX += fontMetrics.charWidth(c);

            }

            // 检查是否需要换行
            if ((i + 1) % charsPerLine == 0 && i < text.length() - 1) {
                // 重置 x 坐标
                currentX = x;

                // 更新 y 坐标
                currentY += fontMetrics.getHeight();
            }
            LimitNumber++;
            if (LimitNumber == fitNumber) {
                break;
            }

        }

    }


    /**
     * 绘制字体颜色等样式
     *
     * @param bufferedImage
     * @param letterGenDTO
     * @return
     */
    public Graphics2D drawWritingStyle(BufferedImage bufferedImage, LetterGenDTO letterGenDTO) {
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        //获取用户选定的字体颜色，字体样式，纸张
        //获取字体颜色，字体 纸张
        //TODO 这里查询了一次数据库
        String fontColor = (fontColorMapper.selectById(letterGenDTO.getFontColorId())).getHexCode();
        String font = (fontMapper.selectById(letterGenDTO.getFontId())).getFilePath();
        String paper = (paperMapper.selectById(letterGenDTO.getPaperId())).getFilePath();

        // 从缓存中获取背景图片
        BufferedImage bgImage = bgImageCache.get(paper);
        if (bgImage == null) {
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("images/Stationery/" + paper);
                if (inputStream == null) {
                    log.error("无法找到指定的图像文件: images/Stationery/{}", paper);
                } else {
                    bgImage = ImageIO.read(inputStream);
                    bgImageCache.put(paper, bgImage); // 缓存背景图片
                }
            } catch (IOException e) {
                log.error("加载背景图片时发生错误: {}", e.getMessage());
            }
        }

        // 背景图适配绘制
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, width, height, null);
        } else {
            log.error("背景图片未找到");
        }
        // 加载自定义字体
        // 从缓存中获取字体
        Font customFont = fontCache.get(font);
        if (customFont == null) {
            try {
                String fontPath = "fonts/MainContent/" + font;
                InputStream fontStream = getClass().getClassLoader().getResourceAsStream(fontPath);
                if (fontStream != null) {
                    customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont((float) 50);
                    fontCache.put(font, customFont); // 缓存字体
                } else {
                    log.error("字体文件未找到: {}", fontPath);
                }

                // 获取本地图形环境并注册字体
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
            } catch (FontFormatException | IOException e) {
                // 如果字体加载失败，使用默认字体
                customFont = new Font("宋体", Font.PLAIN, 50); // 使用支持中文的默认字体，例如宋体
                log.error("加载字体文件时发生错误: {}", e.getMessage());
            }
        }
        // 设置字体及颜色
        g2d.setFont(customFont); // 设置字体
        g2d.setColor(Color.decode(fontColor)); // 设置字体颜色

        return g2d;
    }


}




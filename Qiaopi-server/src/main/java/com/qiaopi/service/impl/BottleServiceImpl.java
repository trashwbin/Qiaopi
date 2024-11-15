package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.BottleGenDTO;
import com.qiaopi.dto.FriendSendDTO;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.*;
import com.qiaopi.exception.bottle.BottleException;
import com.qiaopi.exception.friend.FriendException;
import com.qiaopi.exception.user.UserException;
import com.qiaopi.mapper.*;
import com.qiaopi.service.BottleService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.BottleVo;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class BottleServiceImpl implements BottleService {

    private final UserMapper userMapper;
    private final BottleMapper bottleMapper;
    private final FileStorageService fileStorageService;
    // private final LetterServiceImpl letterService;
    //private Set<Integer> usedIds = new HashSet<>();// 用来存储已经使用过的ID
    private Random random = new Random();

    @Autowired
    private FriendRequestMapper friendRequestMapper;


    /**
     * 漂流瓶生成
     * @param bottleGenDTO
     * @return
     */
    @Override
    public String generateDriftBottle(BottleGenDTO bottleGenDTO) {
        Bottle bottle = BeanUtil.copyProperties(bottleGenDTO, Bottle.class);
        Long userId = null;


        userId = UserContext.getUserId();
        //通过获取到的用户id去获取到用户邮箱和用户昵称
        User user = userMapper.selectById(userId);

        //完善bottle对象的内容
        bottle.setUserId(userId);
        bottle.setEmail(user.getEmail());
        bottle.setNickName(user.getNickname());

        //根据bottle的字体，字体颜色和文本，信纸生成信
        //生成
        String url = generateImage(bottle);
        bottle.setBottleUrl(url);

        //存bottle里面的数据
        bottleMapper.insert(bottle);

        //返回
        return url;
    }



    /**
     * 生成漂流瓶照片
     * @param bottle
     * @return
     */
    public String generateImage(Bottle bottle) {
        // 设置图片的宽和高（根据实际需求可以动态调整）
        int width = 1000; // 图片宽度
        int height = 1500; // 图片高度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像

        //x 增右减左  y 增下减上
        drawMain(g2d, width, height, bottle.getContent(),80, 100);//调用数值使得文本与信纸对齐
        drawNickName(g2d, width, height,  bottle.getNickName(),700, 1300);//调用数值使得文本与信纸对齐

        String url = null;


        try {
            // 将图片写入字节流
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); // 创建字节数组输出流
            ImageIO.write(bufferedImage, "png", baos); // 将BufferedImage写入字节数组输出流
            byte[] imageBytes = baos.toByteArray(); // 获取字节数组

            // 生成一个随机的文件名
            String fileName =  UUID.randomUUID()+ ".png";
            //将照片存储到服务器
            FileInfo fileInfo = fileStorageService.of(imageBytes).setSaveFilename(fileName).setPath("bottle/").upload();
            url = fileInfo.getUrl();
        /*
        // 设置响应头并返回图片
        HttpHeaders headers = new HttpHeaders(); // 创建HttpHeaders对象
        headers.setContentType(MediaType.IMAGE_PNG); // 设置响应内容类型为PNG图片
        headers.setContentLength(imageBytes.length); // 设置响应内容长度
        //return ResponseEntity.ok().headers(headers).body(imageBytes); // 返回包含图片字节数组的响应实体
        */
        } catch (IOException e) {
            log.error("生成图片失败", e);
        }

        return url;
    }



    /**
     * 绘画昵称部分
     */
    public void drawNickName(Graphics2D g2d,  int width, int height, String text,  int x, int y) {

        // 加载自定义字体
        Font customFont = null; // 定义字体对象
        try {
            // 调整字体文件路径以匹配类路径
            String fontPath = "fonts/MainContent/不二情书字体.TTF";

            // 使用类加载器获取字体文件输入流
            InputStream fontStream = getClass().getClassLoader().getResourceAsStream(fontPath);

            if (fontStream != null) {
                // 加载字体文件
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont((float) 50);
            } else {
                log.error("字体文件未找到: " + fontPath);
            }

            // 获取本地图形环境并注册字体
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

        } catch (FontFormatException | IOException e) {
            // 如果字体加载失败，使用默认字体
            customFont = new Font("宋体", Font.PLAIN, 50); // 使用支持中文的默认字体，例如宋体
            log.error("加载字体文件时发生错误: " + e.getMessage());
        }


        // 设置字体及颜色
        g2d.setFont(customFont); // 设置字体

        String color = "#030303";
        g2d.setColor(Color.decode(color)); // 设置字体颜色

        // 获取字体度量信息
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // 计算文本宽度和高度
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        // 绘制文本
        g2d.drawString(text, x, y + textHeight);
    }



    /**
     * 绘画漂流瓶照片文本部分
     */
    public void drawMain(Graphics2D g2d, int width, int height, String text, int x, int y) {
        // 加载背景图片
        BufferedImage bgImage = null;
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("images/Bottle/米黄色花.jpg");
            if (inputStream == null) {
                log.error("无法找到指定的图像文件: images/Bottle/米黄色花.jpg" );
            } else {
                bgImage = ImageIO.read(inputStream);
            }
        } catch (IOException e) {
            log.error("加载背景图片时发生错误: " + e.getMessage());
        }

        // 背景图适配绘制
        g2d.drawImage(bgImage, 0, 0, width, height, null);

        // 加载自定义字体
        Font customFont = null;
        try {
            InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/MainContent/不二情书字体.TTF");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(50f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

        } catch (FontFormatException | IOException e) {
            customFont = new Font("宋体", Font.PLAIN, 50);
            log.error("加载字体文件时发生错误: " + e.getMessage());
        }

        // 设置字体及颜色
        g2d.setFont(customFont);
        g2d.setColor(Color.decode("#030303"));

        // 文本换行和绘制
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int lineHeight = fontMetrics.getHeight();
        List<String> wrappedLines = wrapText(text, fontMetrics, width );

        int currentY = y;
        for (String line : wrappedLines) {
            g2d.drawString(line, x, currentY + fontMetrics.getAscent());
            currentY += lineHeight;
        }

    }


    private static List<String> wrapText(String text, FontMetrics fontMetrics, int maxWidth) {
        List<String> wrappedLines = new ArrayList<>();
        String[] words = text.split(" ");
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
        maxWidth = 800;
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

    /**
     * 字体换行方法
     * @param text
     * @param charsPerLine
     * @return
     */
    private String[] splitTextIntoLines(String text, int charsPerLine) {
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < text.length(); i += charsPerLine) {
            int end = Math.min(i + charsPerLine, text.length());
            lines.add(text.substring(i, end));
        }
        return lines.toArray(new String[0]);

    }






    /**
     * 获取漂流瓶
     * @return
     */
    @Override
    public String getBottle() {
        Long userId = UserContext.getUserId();

        //获取漂流瓶库中合适的漂流瓶
        List<Bottle> bottles = getNotIsPickedBottles();

        if (bottles.isEmpty()) {
            //如果未能取到合适的漂流瓶
            throw new BottleException(MessageUtils.message("bottle.Database.Bottles.empty"));
        }

        try {
            // 随机选择一个未被捡起的漂流瓶
            int index = random.nextInt(bottles.size());
            Bottle bottle = bottles.get(index);

            // 设置更新时间和更新人
            bottle.setUpdateTime(LocalDateTime.now());
            bottle.setUpdateUser(userId);
            bottle.setPicked(true);

            // 更新漂流瓶状态
            bottleMapper.updateById(bottle);

            // 返回照片地址
            return bottle.getBottleUrl();

        } catch (Exception e) {
            throw new BottleException(MessageUtils.message("bottle.get.From.Database.failed") + ": " + e.getMessage());
        }
    }


    /**
     * 获取未被拾起的漂流瓶记录。
     * 该方法用于查询所有未被拾起的漂流瓶，并且排除当前用户创建或更新的漂流瓶。
     * @return 未被拾起的漂流瓶列表
     */
    public List<Bottle> getNotIsPickedBottles() {

        // 使用 LambdaQueryWrapper 构建查询条件
        LambdaQueryWrapper<Bottle> queryWrapper = new LambdaQueryWrapper<Bottle>()
                .eq(Bottle::isPicked, false) // 筛选未被拾起的漂流瓶
                .notIn(Bottle::getUserId, UserContext.getUserId()) // 排除当前用户创建的漂流瓶
                .notIn(Bottle::getUpdateUser, UserContext.getUserId()); // 排除当前用户更新的漂流瓶

        // 执行查询并返回结果
        return bottleMapper.selectList(queryWrapper);
    }



    // 扔回
    @Override
    public void ThrowBack() {
        //获取到当前请求的用户id
        Long currentUserId = UserContext.getUserId();

        try {
            // 获取距离当前时间最近的漂流瓶
            Bottle bottle = getMostRecentBottleByUserId(currentUserId);
            bottle.setPicked(false);

            // 执行更新
            bottleMapper.updateById(bottle);

        } catch (Exception e) {
            throw new BottleException(MessageUtils.message("bottle.throw.back.failed"));
        }


    }

    /**
     * 根据 userId 获取符合条件的 Bottle 对象集合，按 created_time 排序
     * @param userId 用户ID
     * @return Bottle 距离当前时间最近的 Bottle 对象
     */
    public Bottle getMostRecentBottleByUserId(Long userId) {
        // 构建查询条件
        QueryWrapper<Bottle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("update_user", userId)
                //.notIn("update_user",userId)             // 查询条件：update_user 等于传入的 userId
                .orderByDesc("update_time")           // 按 created_time 降序排序
                .last("LIMIT 1");                      // 只取最近的一条记录

        // 查询最近的一条 Bottle 记录
        return bottleMapper.selectOne(queryWrapper);
    }





    @Override
    public void sendFriendRequest(FriendSendDTO friendSendDTO) {
        //获取到当前请求的用户id
        Long currentUserId = UserContext.getUserId();

        // 获取距离当前时间最近的漂流瓶
        Bottle bottle = getMostRecentBottleByUserId(currentUserId);

        if (bottle == null) {
            // 没有找到符合条件的漂流瓶
            throw new BottleException(MessageUtils.message("bottle.not.accord.condition"));
        }

        /*
        // 将 Bottle 转换为 BottleVo 进行返回
        BottleVo bottleVo = convertToBottleVo(bottle);*/

        try {
            // 从最新的 Bottle 对象中获取 userId，并发送好友请求
            Long targetUserId = bottle.getUserId();

            // 插入好友申请
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setSenderId(currentUserId);
            friendRequest.setReceiverId(targetUserId);
            friendRequest.setStatus(0); // 0表示待处理
            friendRequest.setGiveAddress(friendSendDTO.getGiveAddresss());
            friendRequest.setContent(friendSendDTO.getContext());
            friendRequest.setBottleId(bottle.getId());
            friendRequestMapper.insert(friendRequest);
        } catch (Exception e) {
            throw new FriendException(MessageUtils.message("friend.create.Request.failed"));
        }
    }


    /**
     * 将 Bottle 对象转换为 BottleVo 对象
     * @param bottle
     * @return BottleVo
     */
    private BottleVo convertToBottleVo(Bottle bottle) {
        BottleVo bottleVo = new BottleVo();
        bottleVo.setUserId(bottle.getUserId());
        bottleVo.setNickName(bottle.getNickName());
        bottleVo.setEmail(bottle.getEmail());
        bottleVo.setSenderAddress(bottle.getSenderAddress());
        bottleVo.setContent(bottle.getContent());
        bottleVo.setBottleUrl(bottle.getBottleUrl());
        // 其他字段的转换
        return bottleVo;
    }



    /**
     * 根据 update_user 获取符合条件的 Bottle 对象集合
     * @param updateUser 更新用户名称
     * @return List<Bottle> 符合条件的 Bottle 集合
     */
    public List<Bottle> findBottlesByUpdateUser(Long updateUser) {
        // 构建查询条件
        QueryWrapper<Bottle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("update_user", updateUser); // 查询 update_user 等于指定值的数据

        // 执行查询并返回结果
        List<Bottle> bottles = bottleMapper.selectList(queryWrapper);

        // 返回符合条件的 Bottle 集合
        return bottles;
    }





}

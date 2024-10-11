package com.qiaopi.controller.letter;


import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.mapper.FontColorMapper;
import com.qiaopi.mapper.FontMapper;
import com.qiaopi.mapper.PaperMapper;
import com.qiaopi.service.LetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequestMapping("/letter")
@Slf4j
@Tag(name = "书信相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LetterController {


    @Autowired
    private LetterService letterService;

    @Autowired
    private FileStorageService fileStorageService;//注入实列

    @Autowired
    private FontColorMapper fontColorMapper;

    @Autowired
    private FontMapper fontMapper;

    @Autowired
    private PaperMapper paperMapper;


    @PostMapping("/generateImage")
    @Operation(summary = "生成字体照片")
    public ResponseEntity<byte[]> generateImage(
            /*@RequestParam String text,  //文本
            @RequestParam String font, // 字体文件名
            @RequestParam String color, // 字体颜色
            @RequestParam String stationery,// 信纸类型
            @RequestParam String sender,// 寄信人
            @RequestParam String recipient// 收信人*/
            @RequestBody LetterGenDTO letterGenDTO
            ) throws IOException {

        // 设置图片的宽和高（根据实际需求可以动态调整）
        int width = 1000; // 图片宽度
        int height = 1500; // 图片高度

        // 创建一个 BufferedImage 对象
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics(); // 获取Graphics2D对象，用于绘制图像


        ArrayList<String> Parameters = letterService.generationParameters(letterGenDTO);
        String sender = Parameters.get(0);
        String recipient = Parameters.get(1);
        String text = Parameters.get(2);
        String font = Parameters.get(3);
        String color = Parameters.get(4);
        String stationery = Parameters.get(5);


        //进行判断,匹配不同的信纸
        //x调整左右位置，增右减左  y调整上下位置，增下减上
        if (stationery.equals("信纸1")) {
            //第一种信纸
            letterService.drawMain(g2d,text, width, height, color, font,stationery,185,122);//调用数值使得文本与信纸对齐
            letterService.drawSender(g2d, sender, width, height, color, font, stationery, 170, 70);
            letterService.drawRecipient(g2d, recipient, width, height, color, font, stationery, 800, 1370);

        } else if (stationery.equals("信纸2")) {
            //第二种信纸
            letterService.drawMain(g2d, text, width, height, color, font, stationery, 185, 209);
            letterService.drawSender(g2d, sender, width, height, color, font, stationery, 170, 146);
            letterService.drawRecipient(g2d, recipient, width, height, color, font, stationery, 800, 1370);

        } else if (stationery.equals("信纸3")) {
            //第三种信纸
            letterService.drawMain(g2d,text, width, height, color, font,stationery,150,100);//调用数值使得文本与信纸对齐
            letterService.drawSender(g2d, sender, width, height, color, font, stationery, 135, 50);
            letterService.drawRecipient(g2d, recipient, width, height, color, font, stationery, 750, 1400);

        } else if (stationery.equals("4")) {
            //第四种信纸

        } else {
            //第五种信纸
        }

        BufferedImage bufferedImage1 = letterService.rotateImage(bufferedImage, 90);


        // 将图片写入字节流
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // 创建字节数组输出流
        ImageIO.write(bufferedImage1, "png", baos); // 将BufferedImage写入字节数组输出流
        byte[] imageBytes = baos.toByteArray(); // 获取字节数组

        //获取寄信人的名字
        String senderName = sender;

        //将照片存储到服务器
        FileInfo fileInfo = fileStorageService.of(imageBytes).setSaveFilename(senderName).upload();

        // 设置响应头并返回图片
        HttpHeaders headers = new HttpHeaders(); // 创建HttpHeaders对象
        headers.setContentType(MediaType.IMAGE_PNG); // 设置响应内容类型为PNG图片
        headers.setContentLength(imageBytes.length); // 设置响应内容长度

        return ResponseEntity.ok().headers(headers).body(imageBytes); // 返回包含图片字节数组的响应实体
    }


}



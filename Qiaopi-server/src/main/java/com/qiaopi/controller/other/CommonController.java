package com.qiaopi.controller.other;

import com.qiaopi.constant.MessageConstant;
import com.qiaopi.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import static com.qiaopi.constant.MessageConstant.UPLOAD_FAILED;
import static com.qiaopi.constant.MessageConstant.UPLOAD_SUCCESS;
import static com.qiaopi.result.AjaxResult.error;
import static com.qiaopi.result.AjaxResult.success;

/**
 * 通用接口
 */
@RestController
@RequestMapping()
@Tag(name = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private FileStorageService fileStorageService;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public AjaxResult upload(MultipartFile file){
        try {
            log.info("文件上传：{}",file);
            //文件的请求路径

            // 获取上传的文件的输入流
            InputStream inputStream = file.getInputStream();

            // 避免文件覆盖
            String originalFilename = file.getOriginalFilename();
            String extname = originalFilename.substring(originalFilename.lastIndexOf("."));
            //判断文件格式阉割版
            if(extname.isEmpty() || !".jpg".equals(extname) && !".png".equals(extname) && !".jpeg".equals(extname)){
                throw new RuntimeException(MessageConstant.UPLOAD_TYPE_ERROR);
            }
            //文件路径
            String filePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "/" ;

            // 上传文件
            FileInfo fileInfo = fileStorageService.of(file)
                    .setPath(filePath)
                    .setProgressListener((progressSize,allSize) ->
                            log.info("已上传 {} 总大小{}", progressSize, allSize == null ? "未知" : allSize))
                    .upload();

            ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);

            map.put("url", fileInfo.getUrl());
            map.put("fileName", fileInfo.getUrl());//注意这里的fileName是url，因为前端地址会做一个判断，如果以http开头，就直接显示，否则拼接
            map.put("newFileName", fileInfo.getFilename());
            map.put("originalFilename", file.getOriginalFilename());
            return success(UPLOAD_SUCCESS,map);

        }
        catch (Exception e) {
            return error(UPLOAD_FAILED);
            //throw new RuntimeException(e);
        }
    }
}
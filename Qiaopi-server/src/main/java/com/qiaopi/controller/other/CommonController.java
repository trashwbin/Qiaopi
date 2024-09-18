package com.qiaopi.controller.other;

import com.qiaopi.constant.MessageConstant;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ConcurrentHashMap;

import static com.qiaopi.constant.MessageConstant.UPLOAD_FAILED;
import static com.qiaopi.constant.MessageConstant.UPLOAD_SUCCESS;
import static com.qiaopi.result.AjaxResult.error;
import static com.qiaopi.result.AjaxResult.success;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/common")
@Tag(name = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

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
            String filePath = aliOssUtil.upload(file);

            return success(UPLOAD_SUCCESS,filePath);
        } catch (Exception e) {
            return error(UPLOAD_FAILED);
            //throw new RuntimeException(e);
        }
    }
}
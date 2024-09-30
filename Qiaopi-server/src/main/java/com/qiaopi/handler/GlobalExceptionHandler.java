package com.qiaopi.handler;

import com.qiaopi.result.AjaxResult;
import com.qiaopi.constant.MessageConstant;
import com.qiaopi.exception.base.BaseException;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.net.ConnectException;
import java.sql.SQLIntegrityConstraintViolationException;
import static com.qiaopi.result.AjaxResult.error;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public AjaxResult exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return error(ex.getMessage());
    }

    /**
     * 捕获SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public AjaxResult exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("异常信息：{}", ex.getMessage());
        String message = ex.getMessage();
        String msg = "";

        if(!message.contains("Duplicate entry")) {
            return error();
        }
        //一劳永逸的处理方式
        String[] s = message.split(" ");
        msg = s[2] + MessageUtils.message("already.exists");
        return AjaxResult.error(msg);
    }

    @ExceptionHandler
    public AjaxResult exceptionHandler(ConnectException ex){
        log.error("异常信息：{}", ex.getMessage());
        return error(MessageUtils.message("unknown.error"));
    }


}

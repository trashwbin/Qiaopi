package com.qiaopi.enumeration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.qiaopi.constant.AutoFillConstant;
import com.qiaopi.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//注意：这个类加上要加@Component注解，这样spring容器才能管理到
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始进行公共字段自动填充...");
        Long currentId = BaseContext.getCurrentId()==null?0L:BaseContext.getCurrentId();
        this.setFieldValByName(AutoFillConstant.CREATE_TIME, LocalDateTime.now(), metaObject);
        log.info("createTime:{}",metaObject.getValue(AutoFillConstant.CREATE_TIME));
        this.setFieldValByName(AutoFillConstant.UPDATE_TIME, LocalDateTime.now(), metaObject);
        log.info("updateTime:{}",metaObject.getValue(AutoFillConstant.UPDATE_TIME));

        this.setFieldValByName(AutoFillConstant.CREATE_USER, currentId, metaObject);
        log.info("createUser:{}",metaObject.getValue(AutoFillConstant.CREATE_USER));
        this.setFieldValByName(AutoFillConstant.UPDATE_USER, currentId, metaObject);

//        metaObject.setValue(AutoFillConstant.CREATE_TIME, LocalDateTime.now());
//        metaObject.setValue(AutoFillConstant.UPDATE_TIME, LocalDateTime.now());
//        metaObject.setValue(AutoFillConstant.CREATE_USER, BaseContext.getCurrentId());
//        metaObject.setValue(AutoFillConstant.UPDATE_USER, BaseContext.getCurrentId());
        BaseContext.removeCurrentId();
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始进行公共字段自动填充...");
        Long currentId = BaseContext.getCurrentId()==null?0:BaseContext.getCurrentId();
        metaObject.setValue(AutoFillConstant.UPDATE_TIME, LocalDateTime.now());
        metaObject.setValue(AutoFillConstant.UPDATE_USER, currentId);
        BaseContext.removeCurrentId();
    }




}

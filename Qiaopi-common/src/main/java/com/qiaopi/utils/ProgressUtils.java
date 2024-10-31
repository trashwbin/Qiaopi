package com.qiaopi.utils;

import com.qiaopi.entity.Letter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ProgressUtils {
   // 通过开始时间和结束时间计算进度
   public static Letter getProgress(Letter letter) {
       if(letter.getStatus()!=2){
           return letter;
       }
       LocalDateTime startTime = letter.getCreateTime();
       LocalDateTime endTime = letter.getExpectedDeliveryTime();
       LocalDateTime now = LocalDateTime.now();
       long reduceTime = Long.parseLong(letter.getReduceTime());
       double speedRate = Double.parseDouble(letter.getSpeedRate());

       // 计算原始总时间
       long originalTotal = Duration.between(startTime, endTime).toMillis();

       // 计算新的预计送达时间
       long newTotal = (long) ((originalTotal - (reduceTime*60*1000)) / speedRate);
       // 计算当前时间与开始时间的时间差
       long current = Duration.between(startTime, now).toMillis();

       long reduce = originalTotal - newTotal;
       // 计算进度
       long progress = ((current + reduce)* 10000 / originalTotal);

       // 防止进度超过10000
       letter.setDeliveryProgress( progress < 0 || progress > 10000 ? 10000 : progress);
       LocalDateTime deliveryTime = startTime.plus(newTotal, ChronoUnit.MILLIS);
       letter.setDeliveryTime(deliveryTime);
       return letter;
   }
}

package com.qiaopi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.qiaopi.mapper")
public class QiaopiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(QiaopiServerApplication.class, args);
    }

}

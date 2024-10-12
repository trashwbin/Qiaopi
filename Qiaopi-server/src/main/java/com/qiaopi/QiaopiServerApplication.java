package com.qiaopi;

import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@MapperScan("com.qiaopi.mapper")
@EnableFileStorage
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableScheduling
public class QiaopiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(QiaopiServerApplication.class, args);
//        System.out.println("        _                   _ \n" +
//                "   __ _(_) __ _  ___  _ __ (_)\n" +
//                "  / _` | |/ _` |/ _ \\| '_ \\| |\n" +
//                " | (_| | | (_| | (_) | |_) | |\n" +
//                "  \\__, |_|\\__,_|\\___/| .__/|_|\n" +
//                "     |_|             |_|      ");
//        System.out.println("   ___  _                   _ \n" +
//                "  / _ \\(_) __ _  ___  _ __ (_)\n" +
//                " | | | | |/ _` |/ _ \\| '_ \\| |\n" +
//                " | |_| | | (_| | (_) | |_) | |\n" +
//                "  \\__\\_\\_|\\__,_|\\___/| .__/|_|\n" +
//                "                     |_|      ");
//    }

        System.out.println("   ___  _                   _                 _                                               \n" +
                "  / _ \\(_) __ _  ___  _ __ (_)  __      _____| | ___ ___  _ __ ___   ___    _   _  ___  _   _ \n" +
                " | | | | |/ _` |/ _ \\| '_ \\| |  \\ \\ /\\ / / _ \\ |/ __/ _ \\| '_ ` _ \\ / _ \\  | | | |/ _ \\| | | |\n" +
                " | |_| | | (_| | (_) | |_) | |   \\ V  V /  __/ | (_| (_) | | | | | |  __/  | |_| | (_) | |_| |\n" +
                "  \\__\\_\\_|\\__,_|\\___/| .__/|_|    \\_/\\_/ \\___|_|\\___\\___/|_| |_| |_|\\___|   \\__, |\\___/ \\__,_|\n" +
                "                     |_|                                                    |___/             ");
    }




}

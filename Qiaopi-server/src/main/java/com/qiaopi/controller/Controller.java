package com.qiaopi.controller;


import com.qiaopi.utils.ip.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class Controller {
    @GetMapping("/")
    public String home() {
        log.debug("someone visit home :{}", IpUtils.getIpAddr());
        return "<p><span style=\"color:#A52328 ; font-size: 2em;\">The current page is not accessible!</span></p>\n" +
                "<p>Thank you for your support of <span style=\"color:#A52328\">Overseas Chinese Messenger</span>!</p>\n" +
                "<p>Dear <span style=\"color:#A52328\">Overseas Chinese Messenger team</span></p>"; // 假设index是视图的名字
    }
}

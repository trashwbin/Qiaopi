package com.qiaopi.controller.letter;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/letter")
@Slf4j
@Tag(name = "用户相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LetterController {

}

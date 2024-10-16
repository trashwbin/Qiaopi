package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.qiaopi.context.UserContext;
import com.qiaopi.entity.User;
import com.qiaopi.mapper.FontColorMapper;
import com.qiaopi.vo.FontColorShopVO;
import com.qiaopi.vo.FontColorVO;
import com.qiaopi.vo.FontVO;
import com.qiaopi.mapper.FontMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.FontService;
import com.qiaopi.vo.FontShopVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class FontServiceImpl implements FontService {

    private final FontMapper fontMapper;
    private final FontColorMapper fontColorMapper;
    private final UserMapper userMapper;
    @Override
    public List<FontShopVO> list() {

        User user = userMapper.selectById(UserContext.getUserId());
        Map<Long, Long> map = user.getFonts().stream().collect(Collectors.groupingBy(FontVO::getId, Collectors.counting()));

        List<FontShopVO> fontShopVOS = fontMapper.selectList(null).stream().map(font -> {
            FontShopVO fontShopVO = BeanUtil.copyProperties(font, FontShopVO.class);
            fontShopVO.setOwn(map.getOrDefault(font.getId(), 0L) > 0L);
            return fontShopVO;
        }).toList();

        return fontShopVOS;
    }

    @Override
    public List<FontColorShopVO> listColor() {
        User user = userMapper.selectById(UserContext.getUserId());
        Map<Long, Long> map = user.getFontColors().stream().collect(Collectors.groupingBy(FontColorVO::getId, Collectors.counting()));

        List<FontColorShopVO> fontColorShopVOS = fontColorMapper.selectList(null).stream().map(fontColor -> {
            FontColorShopVO fontColorShopVO = BeanUtil.copyProperties(fontColor, FontColorShopVO.class);
            fontColorShopVO.setOwn(map.getOrDefault(fontColor.getId(), 0L) > 0L);
            return fontColorShopVO;
        }).toList();

        return fontColorShopVOS;
    }
}

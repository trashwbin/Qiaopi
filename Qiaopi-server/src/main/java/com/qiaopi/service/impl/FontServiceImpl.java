package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.qiaopi.context.UserContext;
import com.qiaopi.entity.Font;
import com.qiaopi.entity.FontColor;
import com.qiaopi.entity.User;
import com.qiaopi.exception.font.FontException;
import com.qiaopi.exception.user.UserNotExistsException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qiaopi.utils.MessageUtils.message;

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
        if (user == null) {
            return fontMapper.selectList(null).stream().map(font -> BeanUtil.copyProperties(font, FontShopVO.class)).toList();
        }
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
        if (user == null) {
            return fontColorMapper.selectList(null).stream().map(fontColor -> BeanUtil.copyProperties(fontColor, FontColorShopVO.class)).toList();
        }
        Map<Long, Long> map = user.getFontColors().stream().collect(Collectors.groupingBy(FontColorVO::getId, Collectors.counting()));

        List<FontColorShopVO> fontColorShopVOS = fontColorMapper.selectList(null).stream().map(fontColor -> {
            FontColorShopVO fontColorShopVO = BeanUtil.copyProperties(fontColor, FontColorShopVO.class);
            fontColorShopVO.setOwn(map.getOrDefault(fontColor.getId(), 0L) > 0L);
            return fontColorShopVO;
        }).toList();

        return fontColorShopVOS;
    }

    @Override
    @Transactional
    public void buyFont(Long fontId) {
        Font font = fontMapper.selectById(fontId);
        if (font == null) {
            throw new FontException(message("font.not.exists"));
        }
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        List<FontVO> fonts = user.getFonts();
        if (fonts == null) {
            fonts = List.of();
        }
        for (FontVO fontVO : fonts) {
            if (fontVO.getId().equals(fontId)) {
                throw new FontException(message("font.own"));
            }
        }
        if (user.getMoney() < font.getPrice()) {
            throw new FontException(message("user.money.not.enough"));
        }else {
            user.setMoney(user.getMoney() - font.getPrice());
        }

        FontVO fontVO = BeanUtil.copyProperties(font, FontVO.class);
        fonts.add(fontVO);
        user.setFonts(fonts);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void buyFontColor(Long fontColorId) {
        FontColor fontColor = fontColorMapper.selectById(fontColorId);
        if (fontColor == null) {
            throw new FontException(message("font.color.not.exists"));
        }
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        List<FontColorVO> fontColors = user.getFontColors();
        if (fontColors == null) {
            fontColors = List.of();
        }
        for (FontColorVO fontColorVO : fontColors) {
            if (fontColorVO.getId().equals(fontColorId)) {
                throw new FontException(message("font.color.own"));
            }
        }
        if (user.getMoney() < fontColor.getPrice()) {
            throw new FontException(message("user.money.not.enough"));
        }else {
            user.setMoney(user.getMoney() - fontColor.getPrice());
        }
        FontColorVO fontColorVO = BeanUtil.copyProperties(fontColor, FontColorVO.class);
        fontColors.add(fontColorVO);
        user.setFontColors(fontColors);
        userMapper.updateById(user);
    }
}

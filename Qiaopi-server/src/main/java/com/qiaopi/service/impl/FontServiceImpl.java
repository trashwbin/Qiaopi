package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.qiaopi.context.UserContext;
import com.qiaopi.entity.Font;
import com.qiaopi.entity.FontColor;
import com.qiaopi.entity.User;
import com.qiaopi.exception.font.FontException;
import com.qiaopi.exception.user.UserNotExistsException;
import com.qiaopi.mapper.FontColorMapper;
import com.qiaopi.service.UserService;
import com.qiaopi.vo.FontColorShopVO;
import com.qiaopi.vo.FontColorVO;
import com.qiaopi.vo.FontVO;
import com.qiaopi.mapper.FontMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.FontService;
import com.qiaopi.vo.FontShopVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.qiaopi.constant.CacheConstant.*;
import static com.qiaopi.utils.MessageUtils.message;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class FontServiceImpl implements FontService {

    private final FontMapper fontMapper;
    private final FontColorMapper fontColorMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;
@Override
public List<FontShopVO> list() {
    Long userId = UserContext.getUserId();

    // 从Redis中获取字体列表
    List<FontShopVO> fontShopVOS = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_SHOP_FONT_KEY), FontShopVO.class);
    if (CollUtil.isEmpty(fontShopVOS)) {
        fontShopVOS = fontMapper.selectList(null).stream().map(font -> BeanUtil.copyProperties(font, FontShopVO.class)).toList();
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_FONT_KEY, JSONUtil.toJsonStr(fontShopVOS), Duration.ofHours(24));
    }
    if (userId == null) {
        return fontShopVOS;
    }
    // 设置用户是否拥有这个字体
    ConcurrentHashMap repository = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(CACHE_USER_REPOSITORY_KEY + userId), ConcurrentHashMap.class);
    List<FontVO> userFonts = repository.get("fonts") == null ? Collections.emptyList() : JSONUtil.toList(JSONUtil.toJsonStr(repository.get("fonts")), FontVO.class);
    if (CollUtil.isEmpty(userFonts)) {
        // 这个查询自动会存Redis
        repository = userService.getUserRepository(userId);
        userFonts = repository.get("fonts") == null ? Collections.emptyList() : JSONUtil.toList(JSONUtil.toJsonStr(repository.get("fonts")), FontVO.class);
    }
    Map<Long, Long> map = userFonts.stream().collect(Collectors.groupingBy(FontVO::getId, Collectors.counting()));
    fontShopVOS.forEach(fontShopVO -> {
        fontShopVO.setOwn(map.getOrDefault(fontShopVO.getId(), 0L) > 0L);
    });

    return fontShopVOS;
}

    @Override
    public List<FontColorShopVO> listColor() {
        Long userId = UserContext.getUserId();

        // 从Redis中获取字体颜色列表
        List<FontColorShopVO> fontColorShopVOS = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_SHOP_FONT_COLOR_KEY), FontColorShopVO.class);
        if(CollUtil.isEmpty(fontColorShopVOS)){
            fontColorShopVOS = fontColorMapper.selectList(null).stream().map(fontColor -> BeanUtil.copyProperties(fontColor, FontColorShopVO.class)).toList();
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_FONT_COLOR_KEY, JSONUtil.toJsonStr(fontColorShopVOS),Duration.ofHours(24));
        }
        if (userId == null) {
            return fontColorShopVOS;
        }
        ConcurrentHashMap repository = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(CACHE_USER_REPOSITORY_KEY + userId), ConcurrentHashMap.class);
        List<FontColorVO> userFontColors = repository.get("fontColors") == null ? Collections.emptyList() : JSONUtil.toList(JSONUtil.toJsonStr(repository.get("fontColors")), FontColorVO.class);
        if (CollUtil.isEmpty(userFontColors)) {
            // 这个查询自动会存Redis
            repository = userService.getUserRepository(userId);
            userFontColors = repository.get("fontColors") == null ? Collections.emptyList() : JSONUtil.toList(JSONUtil.toJsonStr(repository.get("fontColors")), FontColorVO.class);
        }
        Map<Long, Long> map = userFontColors.stream().collect(Collectors.groupingBy(FontColorVO::getId, Collectors.counting()));
        fontColorShopVOS.forEach(fontColorShopVO -> {
            fontColorShopVO.setOwn(map.getOrDefault(fontColorShopVO.getId(), 0L) > 0L);
        });

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
        stringRedisTemplate.delete(CACHE_USER_REPOSITORY_KEY + UserContext.getUserId());
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
        stringRedisTemplate.delete(CACHE_USER_REPOSITORY_KEY + UserContext.getUserId());
    }


}

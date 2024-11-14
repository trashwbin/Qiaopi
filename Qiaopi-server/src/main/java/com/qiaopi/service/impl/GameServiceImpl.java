package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.qiaopi.context.UserContext;
import com.qiaopi.entity.FontPaper;
import com.qiaopi.entity.Paper;
import com.qiaopi.entity.User;
import com.qiaopi.exception.paper.PaperException;
import com.qiaopi.exception.user.UserException;
import com.qiaopi.exception.user.UserNotExistsException;
import com.qiaopi.mapper.FontPaperMapper;
import com.qiaopi.mapper.PaperMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.GameService;
import com.qiaopi.service.PaperService;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.PaperShopVO;
import com.qiaopi.vo.PaperVO;
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

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class GameServiceImpl implements GameService {

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void winFfl(Long userId) {
        Integer limit= JSON.parseObject(stringRedisTemplate.opsForValue().get(GAME_FFL_USER_KEY + userId), Integer.class);
        if (limit == null || limit <= 0) {
            throw new UserException("ffl.limit.error");
        }
        limit--;
        stringRedisTemplate.opsForValue().set(GAME_FFL_USER_KEY + userId, JSON.toJSONString(limit), Duration.ofDays(1));
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }
        user.setMoney(user.getMoney() + 10);
        userMapper.updateById(user);
    }

    @Override
    public Integer getFflLimit(Long userId) {
        Integer limit= JSON.parseObject(stringRedisTemplate.opsForValue().get(GAME_FFL_USER_KEY + userId), Integer.class);
        if (limit == null) {
            limit = 10;
            stringRedisTemplate.opsForValue().set(GAME_FFL_USER_KEY + userId, JSON.toJSONString(limit), Duration.ofDays(1));
        }
        return limit;
    }
}

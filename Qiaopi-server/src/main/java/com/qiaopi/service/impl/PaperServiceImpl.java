package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.qiaopi.context.UserContext;
import com.qiaopi.entity.FontColor;
import com.qiaopi.entity.FontPaper;
import com.qiaopi.entity.Paper;
import com.qiaopi.entity.User;
import com.qiaopi.exception.font.FontException;
import com.qiaopi.exception.paper.PaperException;
import com.qiaopi.exception.user.UserNotExistsException;
import com.qiaopi.mapper.FontPaperMapper;
import com.qiaopi.mapper.PaperMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.FontService;
import com.qiaopi.service.PaperService;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.*;
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
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final UserMapper userMapper;
    private final FontPaperMapper fontPaperMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;

@Override
public List<PaperShopVO> list() {
    Long userId = UserContext.getUserId();

    // 从Redis中获取纸张列表
    List<PaperShopVO> paperShopVOS = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_SHOP_PAPER_KEY), PaperShopVO.class);
    if (CollUtil.isEmpty(paperShopVOS)) {
        paperShopVOS = paperMapper.selectList(null).stream().map(paper -> BeanUtil.copyProperties(paper, PaperShopVO.class)).toList();
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_PAPER_KEY, JSONUtil.toJsonStr(paperShopVOS), Duration.ofHours(24));
    }
    if (userId == null) {
        return paperShopVOS;
    }
    ConcurrentHashMap repository = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(CACHE_USER_REPOSITORY_KEY + userId), ConcurrentHashMap.class);
    List<PaperVO> userPapers = repository.get("papers") == null ? Collections.emptyList() : JSONUtil.toList(JSONUtil.toJsonStr(repository.get("papers")), PaperVO.class);
    if (CollUtil.isEmpty(userPapers)) {
        // 这个查询自动会存Redis
        repository = userService.getUserRepository(userId);
        userPapers = repository.get("papers") == null ? Collections.emptyList() : JSONUtil.toList(JSONUtil.toJsonStr(repository.get("papers")), PaperVO.class);
    }
    Map<Long, Long> map = userPapers.stream().collect(Collectors.groupingBy(PaperVO::getId, Collectors.counting()));
    paperShopVOS.forEach(paperShopVO -> {
        paperShopVO.setOwn(map.getOrDefault(paperShopVO.getId(), 0L) > 0L);
    });

    return paperShopVOS;
}

    @Override
    @Transactional
    public void buyPaper(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new PaperException(MessageUtils.message("paper.not.exists"));
        }
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        List<PaperVO> papers = user.getPapers();
        if (papers==null){
            papers = List.of();
        }
        for (PaperVO paperVO : papers) {
            if (paperVO.getId().equals(paperId)) {
                throw new PaperException(MessageUtils.message("paper.own"));
            }
        }
        if (user.getMoney() < paper.getPrice()) {
            throw new PaperException(MessageUtils.message("paper.not.enough"));
        }else{
            user.setMoney(user.getMoney() - paper.getPrice());
        }
        PaperVO paperVO = BeanUtil.copyProperties(paper, PaperVO.class);
        papers.add(paperVO);
        user.setPapers(papers);
        userMapper.updateById(user);
        stringRedisTemplate.delete(CACHE_USER_REPOSITORY_KEY + UserContext.getUserId());
    }

    @Override
    public List<FontPaper> getFontPaperLimit() {
        List<FontPaper> limitList = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_WORD_LIMIT_KEY), FontPaper.class);
        if (CollUtil.isEmpty(limitList)) {
            limitList = fontPaperMapper.selectList(null);
            stringRedisTemplate.opsForValue().set(CACHE_WORD_LIMIT_KEY, JSONUtil.toJsonStr(limitList),Duration.ofHours(24));
        }
        return limitList;
    }
}

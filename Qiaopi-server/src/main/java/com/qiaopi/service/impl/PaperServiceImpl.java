package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.qiaopi.context.UserContext;
import com.qiaopi.entity.User;
import com.qiaopi.mapper.PaperMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.FontService;
import com.qiaopi.service.PaperService;
import com.qiaopi.vo.FontShopVO;
import com.qiaopi.vo.FontVO;
import com.qiaopi.vo.PaperShopVO;
import com.qiaopi.vo.PaperVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final UserMapper userMapper;

    @Override
    public List<PaperShopVO> list() {
        User user = userMapper.selectById(UserContext.getUserId());
        Map<Long, Long> map = user.getPapers().stream().collect(Collectors.groupingBy(PaperVO::getId, Collectors.counting()));

        List<PaperShopVO> paperShopVOS = paperMapper.selectList(null).stream().map(paper -> {
            PaperShopVO paperShopVO = BeanUtil.copyProperties(paper, PaperShopVO.class);
            paperShopVO.setOwn(map.getOrDefault(paper.getId(), 0L) > 0L);
            return paperShopVO;
        }).toList();
        return paperShopVOS;
    }
}

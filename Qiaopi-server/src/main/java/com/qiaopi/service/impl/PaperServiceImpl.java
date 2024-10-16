package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.qiaopi.context.UserContext;
import com.qiaopi.entity.FontColor;
import com.qiaopi.entity.Paper;
import com.qiaopi.entity.User;
import com.qiaopi.exception.font.FontException;
import com.qiaopi.exception.paper.PaperException;
import com.qiaopi.exception.user.UserNotExistsException;
import com.qiaopi.mapper.PaperMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.FontService;
import com.qiaopi.service.PaperService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    }
}

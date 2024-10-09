package com.qiaopi;

import cn.hutool.core.bean.BeanUtil;
import com.qiaopi.entity.*;
import com.qiaopi.mapper.*;
import com.qiaopi.vo.FontColorVO;
import com.qiaopi.vo.FontVO;
import com.qiaopi.vo.PaperVO;
import com.qiaopi.vo.SignetVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class QiaopiServerApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FontMapper fontMapper;
    @Autowired
    private FontColorMapper fontColorMapper;
    @Autowired
    private PaperMapper paperMapper;
    @Autowired
    private SignetMapper signetMapper;
    @Test
    void add() {
        User user = userMapper.selectById(1L);
        List<FontVO> fonts = BeanUtil.copyToList(fontMapper.selectList(null), FontVO.class);;

        List<FontColorVO> fontColors =  BeanUtil.copyToList(fontColorMapper.selectList(null), FontColorVO.class);

        List<PaperVO> papers =  BeanUtil.copyToList(paperMapper.selectList(null), PaperVO.class);
        List<SignetVO> signets = BeanUtil.copyToList(signetMapper.selectList(null), SignetVO.class);
        user.setFontColors(fontColors);
        user.setFonts(fonts);
        user.setPapers(papers);
        user.setSignets(signets);
        userMapper.updateById(user);
    }

}

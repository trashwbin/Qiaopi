package com.qiaopi;

import cn.hutool.core.bean.BeanUtil;
import com.qiaopi.entity.*;
import com.qiaopi.mapper.*;
import com.qiaopi.vo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Autowired
    private CardMapper cardMapper;
    @Test
    void add() {



        List<FontVO> fonts = BeanUtil.copyToList(fontMapper.selectList(null), FontVO.class);;

        List<FontColorVO> fontColors =  BeanUtil.copyToList(fontColorMapper.selectList(null), FontColorVO.class);

        List<PaperVO> papers =  BeanUtil.copyToList(paperMapper.selectList(null), PaperVO.class);
        List<SignetVO> signets = BeanUtil.copyToList(signetMapper.selectList(null), SignetVO.class);

        List<FunctionCardVO> functionCards = BeanUtil.copyToList(cardMapper.selectList(null), FunctionCardVO.class);
        functionCards.forEach(card -> {
            card.setNumber(999);
        });
        User user = new User();
        user.setId(1L);
        user.setFontColors(fontColors);
        user.setFonts(fonts);
        user.setPapers(papers);
        user.setSignets(signets);
        user.setFunctionCards(functionCards);
        userMapper.updateById(user);

        FontColor fontColor = fontColorMapper.selectById(1L);
        String hexCode = fontColor.getHexCode();

    }

    @Test
    void contextLoads() {
        String[] addresses = {
                "北京市东城区东华门街道天安门",
                "山西省大同市灵丘县赵北乡辛安庄村",
                "内蒙古自治区锡林郭勒盟苏尼特右旗桑宝拉格苏木",
                "内蒙古自治区乌兰察布市察哈尔右翼前旗平地泉镇",
                "香港特别行政区中西区马己仙峡道28号",
                "广西壮族自治区南宁市武鸣区太平镇034乡道",
                "上海市嘉定区江桥镇海波路878号中星海上名豪苑四期",
                "浙江省湖州市吴兴区环渚街道福莱福路1号环渚龙泉街道社区卫生服务中心",
                "黑龙江省大兴安岭地区呼玛县碧水镇"
        };

        for (String address : addresses) {
            System.out.println(parseAddress(address));
        }
    }


    public static String parseAddress(String address) {
        // 匹配省份或直辖市名称
        Pattern patternProvince = Pattern.compile("(北京|天津|上海|重庆|香港|澳门|\\S*?自治区|\\S*?省)");
        Matcher matcherProvince = patternProvince.matcher(address);

        // 匹配城市名称
        Pattern patternCity = Pattern.compile("(\\S*市)");
        Matcher matcherCity = patternCity.matcher(address);

        if (matcherProvince.find()) {
            String province = matcherProvince.group();
            // 去除“省”、“自治区”等后缀
            province = province.replaceAll("(省|自治区|\\s*)$", ""); // 修正正则表达式

            if (matcherCity.find()) {
                String city = matcherCity.group(1);
                // 如果城市名称长度大于3，则返回省份
                if (city.length() <= 3) {
                    return city;
                }
            }
            return province;
        }
        return address; // 如果没有找到匹配项，则返回原地址
    }
}

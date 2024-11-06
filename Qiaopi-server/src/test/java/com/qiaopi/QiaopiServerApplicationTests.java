package com.qiaopi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.json.JSONUtil;
import com.qiaopi.entity.*;
import com.qiaopi.mapper.*;
import com.qiaopi.utils.AESUtil;
import com.qiaopi.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qiaopi.constant.CacheConstant.*;

@SpringBootTest
@Slf4j
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
    @Autowired
    private LetterMapper letterMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void sign(){
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.getDayOfWeek().getValue());
        String prefix = stringRedisTemplate.opsForValue().get("sign:current");
        stringRedisTemplate.opsForValue().setBit("sign:"+prefix+":user-1", now.getDayOfWeek().getValue()-1, true);
        String s = stringRedisTemplate.opsForValue().get("sign:" + prefix + ":user-1");
        System.out.println(s);
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                "sign:" + prefix + ":user-1",
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(now.getDayOfWeek().getValue())).valueAt(0)
        );
        System.out.println(result);
        Long num = result.get(0);
        if (num == null || num == 0) {
            // 6.如果为0，说明未签到，结束
            System.out.println("未签到");
        }
        int count = 0;
        String s1= Long.toBinaryString(num);
        System.out.println(s1);
        while (true) {
            // 6.1.让这个数字与1做与运算，得到数字的最后一个bit位  // 判断这个bit位是否为0
            if ((num & 1) == 0) {
                // 如果为0，说明未签到，结束
                break;
            }else {
                // 如果不为0，说明已签到，计数器+1
                count++;
            }
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }
        System.out.println(count);
    }
    @Test
    public void deleteTodaySignCache(){
        log.info("删除今日签到缓存");
        Set<String> keys = stringRedisTemplate.keys(SIGN_TODAY_ALL_KEY);
        assert keys != null;
        System.out.println(stringRedisTemplate.delete(keys));
        //stringRedisTemplate.opsForValue().set("sign:today:user", "1");
    }
    @Test
    void buildSignTable(){
        List<UserSignAward> userSignAwardList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            UserSignAward userSignAward = new UserSignAward();
            userSignAward.setId((long) i);
            userSignAward.setAwardType("1");
            userSignAward.setAwardDesc("猪仔钱");
            userSignAward.setAwardName("猪仔钱");
            userSignAward.setAwardNum(1);
            userSignAward.setPreviewLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/card/0.webp");
            userSignAward.setSignDays(i + 1);
            userSignAwardList.add(userSignAward);
        }
        LocalDateTime now = LocalDateTime.now();
        String prefix = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        stringRedisTemplate.opsForValue().set("sign:"+prefix, JSONUtil.toJsonStr(userSignAwardList));
        stringRedisTemplate.opsForValue().set("sign:current", prefix);
    }

    @Test
    void trimLetters(){
        letterMapper.selectList(null).forEach(letter -> {
            letter.setLetterContent(letter.getLetterContent().trim().replaceAll("\n", ""));
            letterMapper.updateById(letter);
        });
    }

    @Test
    void encode() throws Exception {
        String string = AESUtil.encrypt("hello", "12345678901234567890123456789012");
        System.out.println(string);
    }

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

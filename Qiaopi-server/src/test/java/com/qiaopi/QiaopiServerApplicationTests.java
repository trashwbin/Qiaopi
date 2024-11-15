package com.qiaopi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.qiaopi.entity.*;
import com.qiaopi.handler.Ai.pojo.AiInteractData;
import com.qiaopi.mapper.*;
import com.qiaopi.service.ChatService;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qiaopi.constant.CacheConstant.*;
import static com.qiaopi.constant.AiConstant.*;

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
    @Autowired
    private ChatService chatService;
    @Test
    void AiInteractInit(){
        String message = "别忘了参加‘侨趣乐园’中的游戏，通过趣味的方式学习侨批文化，还能赢取‘猪仔钱’哦！💰";
        String router = "game";
        AiInteractData aiInteractData = new AiInteractData(message, router);
        List<AiInteractData> list = new ArrayList<>();
        list.add(aiInteractData);
        stringRedisTemplate.opsForValue().set(INTERACTIVE_LIST, JSON.toJSONString(list));
    }

    @Test
    void sign(){
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.getDayOfWeek().getValue());
        String prefix = "signed" + stringRedisTemplate.opsForValue().get("sign:current");
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
        Set<String> keys = stringRedisTemplate.keys(SIGN_TODAY_KEY);
        assert keys != null;
        System.out.println(stringRedisTemplate.delete(keys));
        //stringRedisTemplate.opsForValue().set("sign:today:user", "1");
    }
/*
    @Test
    void buildSignTable(){
        log.info("刷新签到奖励");
        List<UserSignAward> userSignAwardList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            UserSignAward userSignAward = new UserSignAward();
            userSignAward.setId((long) i+1);
            userSignAward.setAwardType(1);
            userSignAward.setAwardDesc("猪仔钱");
            userSignAward.setAwardName("猪仔钱");
            userSignAward.setAwardNum((i+1)*10);
            userSignAward.setAwardId(0L);
//            userSignAward.setPreviewLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/card/0.webp");
            userSignAward.setPreviewLink("data:image/webp;base64,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\n");
            //data:image/webp;base64,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
            userSignAward.setSignDays(i + 1);
            userSignAwardList.add(userSignAward);
        }
        FunctionCard functionCard = cardMapper.selectById(2L);
        userSignAwardList.set(1,UserSignAward.builder().id(2L).signDays(2).previewLink(functionCard.getCardPreviewLink()).awardType(2).awardName(functionCard.getCardName()).awardDesc(functionCard.getCardDesc()).awardNum(1).awardId(functionCard.getId()).build());
        functionCard = cardMapper.selectById(8L);
        userSignAwardList.set(3,UserSignAward.builder().id(4L).signDays(4).previewLink(functionCard.getCardPreviewLink()).awardType(2).awardName(functionCard.getCardName()).awardDesc(functionCard.getCardDesc()).awardNum(1).awardId(functionCard.getId()).build());
        functionCard = cardMapper.selectById(0);
        userSignAwardList.set(5,UserSignAward.builder().id(6L).signDays(6).previewLink(functionCard.getCardPreviewLink()).awardType(2).awardName(functionCard.getCardName()).awardDesc(functionCard.getCardDesc()).awardNum(1).awardId(functionCard.getId()).build());

        userSignAwardList.set(6,UserSignAward.builder().id(7L).signDays(7).previewLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/font/06.png").awardType(3).awardName("随机字体").awardDesc("随机获得您未拥有的字体字体").awardNum(1).awardId(0L).build());

        LocalDateTime now = LocalDateTime.now();
        String prefix = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        stringRedisTemplate.opsForValue().set(SIGN_AWARD_KEY + prefix, JSONUtil.toJsonStr(userSignAwardList));
        stringRedisTemplate.opsForValue().set(SIGN_CURRENT_KEY, prefix);
    }
*/

    @Test
    void buildTaskTable(){
        log.info("每日任务");
        List<TaskTable> taskTableList = new ArrayList<>();

        TaskTable taskTable1 = new TaskTable();
        taskTable1.setId(1L);
        taskTable1.setTaskName("执笔传情");
        taskTable1.setDescription("拿起笔，写下你的真心话，让这份温暖传递到每一个角落。去写下一封信吧！！");
        taskTable1.setStatus(0);
        taskTable1.setMoney(20);
        taskTable1.setLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/task/1.webp");
        taskTable1.setRoute("/write");

        TaskTable taskTable2 = new TaskTable();
        taskTable2.setId(2L);
        taskTable2.setTaskName("海上信使");
        taskTable2.setDescription("漂流瓶如同一叶扁舟，载着梦想与希望，漂向未知的彼岸。投下一个漂流瓶吧！");
        taskTable2.setStatus(0);
        taskTable2.setMoney(10);
        taskTable2.setLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/task/2.webp");
        taskTable2.setRoute("/drifting");


        TaskTable taskTable3 = new TaskTable();
        taskTable3.setId(3L);
        taskTable3.setTaskName("智海探宝");
        taskTable3.setDescription("答题如同一场智力的盛宴，激发思维的火花，带来知识的满足与成长。来解答一道题目吧！");
        taskTable3.setStatus(0);
        taskTable3.setMoney(10);
        taskTable3.setLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/task/3.webp");
        taskTable3 .setRoute("/know");

        TaskTable taskTable4 = new TaskTable();
        taskTable4.setId(4L);
        taskTable4.setTaskName("翻牌寻趣");
        taskTable4.setDescription("翻翻乐如同一扇神秘的窗，每一次翻开都带来新的惊喜与欢乐。来体验一次翻翻乐吧！");
        taskTable4.setStatus(0);
        taskTable4.setMoney(10);
        taskTable4.setLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/task/4.webp");
        taskTable4.setRoute("/memory");

        TaskTable taskTable5 = new TaskTable();
        taskTable5.setId(5L);
        taskTable5.setTaskName("结缘识友");
        taskTable5.setDescription("在这个数字化的世界里，结交新朋友如同一束阳光，照亮彼此的心房，带来温暖与快乐！");
        taskTable5.setStatus(0);
        taskTable5.setMoney(10);
        taskTable5.setLink("http://110.41.58.26:9000/qiaopi/qiaopi-images/task/5.webp");
        taskTable5.setRoute("/drifting");

        taskTableList.add(taskTable1);
        taskTableList.add(taskTable2);
        taskTableList.add(taskTable3);
        taskTableList.add(taskTable4);
        taskTableList.add(taskTable5);

        //接下来把taskTableList存入redis中
        stringRedisTemplate.opsForValue().set(TASK_TABLE_KEY , JSONUtil.toJsonStr(taskTableList));

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

package com.qiaopi.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.qiaopi.constant.JwtClaimsConstant;
import com.qiaopi.constant.UserConstants;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.dto.UserResetPasswordDTO;
import com.qiaopi.dto.UserUpdateDTO;
import com.qiaopi.entity.*;
import com.qiaopi.exception.base.BaseException;
import com.qiaopi.exception.card.CardException;
import com.qiaopi.exception.code.CodeErrorException;
import com.qiaopi.exception.code.CodeTimeoutException;
import com.qiaopi.exception.friend.FriendException;
import com.qiaopi.exception.friend.FriendNotExistsException;
import com.qiaopi.exception.user.*;
import com.qiaopi.mapper.*;
import com.qiaopi.properties.JwtProperties;
import com.qiaopi.service.CardService;
import com.qiaopi.service.FontService;
import com.qiaopi.service.LetterService;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.AccountValidator;
import com.qiaopi.utils.JwtUtil;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.utils.StringUtils;
import com.qiaopi.utils.ip.IpUtils;
import com.qiaopi.vo.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static cn.hutool.core.bean.BeanUtil.copyProperties;
import static com.qiaopi.constant.CacheConstant.*;
import static com.qiaopi.constant.MqConstant.EXCHANGE_AI_DIRECT;
import static com.qiaopi.constant.MqConstant.ROUTING_KEY_SIGN_AWARD;
import static com.qiaopi.result.AjaxResult.error;
import static com.qiaopi.result.AjaxResult.success;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.qiaopi.utils.MessageUtils.message;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;
    private final FriendMapper friendMapper;
    private final AvatarMapper avatarMapper;
    private final FontColorMapper fontColorMapper;
    private final FontMapper fontMapper;
    private final PaperMapper paperMapper;
    private final CardMapper cardMapper;
    private final JwtProperties jwtProperties;
    private final RedisTemplate redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final LetterMapper letterMapper;
    private final LetterService letterService;
    private final CountryMapper countryMapper;
    private final RabbitTemplate rabbitTemplate;


    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {

        //检验验证码是否正确
        //从redis中获取验证码
        String code = (String) redisTemplate.opsForValue().get(userLoginDTO.getUuid());
        if (code == null) {
            //验证码已过期
            throw new CodeTimeoutException();
            //} else if (!code.equals(userLoginDTO.getCode())) {
        } else if (!code.equalsIgnoreCase(userLoginDTO.getCode())) {
            //验证码不匹配
            throw new CodeErrorException();
        }
        // 根据用户名查询用户信息
        if (!AccountValidator.isValidAccount(userLoginDTO.getUsername())) {
            //用户名不合法
            throw new UserNameNotMatchException();
        }
        if (StringUtils.isEmpty(userLoginDTO.getPassword())) {
            //密码为空
            throw new UserPasswordNotMatchException();
        }
        redisTemplate.delete(userLoginDTO.getUuid());
        //为了方便查询，将用户名和密码封装到User对象中
        //对前端传过来的明文密码进行MD5加密处理
        User userLogin = User.builder().password(DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes())).build();

        if (AccountValidator.isValidEmail(userLoginDTO.getUsername())) {
            userLogin.setEmail(userLoginDTO.getUsername().toLowerCase());
        } else {
            userLogin.setUsername(userLoginDTO.getUsername());
        }

        //根据用户名和密码查询用户信息的条件构造器
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (userLogin.getUsername() != null) {
            wrapper
                    .eq(User::getUsername, userLogin.getUsername())
                    .eq(User::getPassword, userLogin.getPassword());
        } else {
            wrapper
                    .eq(User::getEmail, userLogin.getEmail())
                    .eq(User::getPassword, userLogin.getPassword());
        }
        //根据条件查询用户信息,mybatis-plus会自动查询，封装到User对象中
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            //账号不存在
            throw new UserLoginNotExistsException();
        }

        //更新最后登录时间，ip地址
        user.setLoginDate(LocalDateTime.now());
        user.setLoginIp(IpUtils.getIpAddr());
        userMapper.updateById(user);

        //生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        return UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public String register(UserRegisterDTO userRegisterDTO) {

        String msg = "", email = userRegisterDTO.getUsername().toLowerCase(), password = userRegisterDTO.getPassword();
        User user = new User();
        user.setEmail(email);

        //一系列检验
        if (StringUtils.isEmpty(email)) {
            msg = message("user.email.empty");
        } else if (!AccountValidator.isValidEmail(email)) {
            msg = message("email.format.error");
        } else if (StringUtils.isEmpty(password)) {
            msg = message("user.password.empty");
        } else if (userRegisterDTO.getCode() == null) {
            msg = message("user.code.empty");
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = message("user.password.length");
        } else if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            msg = message("user.password.confirm.error");
        } else if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) != null) {
            msg = email + message("email.exists");
        }
        String emailKey = message("user.register.prefix") + email;

        String code = (String) redisTemplate.opsForValue().get(emailKey);

        if (StringUtils.isEmpty(code)) {
            msg = message("user.code.expire");
        } else if (!code.equals(userRegisterDTO.getCode())) {
            msg = message("user.code.error");
        }
        //检验是否通过，如果不通过，抛出异常
        if (!StringUtils.isEmpty(msg)) {
            throw new UserException(msg);
        }
        //删除验证码
        redisTemplate.delete(emailKey);
        //设置昵称
//        user.setNickname(email.substring(0, email.indexOf("@")));
        user.setNickname("侨宝");
        //设置用户名
        user.setUsername(message("user.username.prefix") + System.currentTimeMillis() + getStringRandom(3));
        //设置密码
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));

        //注册后添加默认参数
        //设置默认头像
        user.setAvatar(avatarMapper.selectById(1).getUrl());
        //设置默认性别
        user.setSex("男");
        //设置默认字体颜色
        user.setFontColors(Collections.singletonList(BeanUtil.copyProperties(fontColorMapper.selectById(1), FontColorVO.class)));

        List<FontVO>  fonts= new ArrayList<>();
        fonts.add(BeanUtil.copyProperties(fontMapper.selectById(1), FontVO.class));
        fonts.add(BeanUtil.copyProperties(fontMapper.selectById(2), FontVO.class));
        //设置默认字体
        user.setFonts(fonts);
        //设置默认纸张
        List<PaperVO> papers = new ArrayList<>();
        papers.add(BeanUtil.copyProperties(paperMapper.selectById(1), PaperVO.class));
        papers.add(BeanUtil.copyProperties(paperMapper.selectById(4), PaperVO.class));
        user.setPapers(papers);
        //设置默认功能卡
        FunctionCard functionCard = cardMapper.selectById(0L);
        FunctionCardVO functionCardVO = copyProperties(functionCard, FunctionCardVO.class);
        functionCardVO.setNumber(1);
        user.setFunctionCards(Collections.singletonList(functionCardVO));
        //设置默认印章
        user.setSignets(Collections.emptyList());
        //设置默认地址
        user.setAddresses(Collections.emptyList());
        //发送邮件
        Letter letter = letterMapper.selectById(1);
        letter.setRecipientEmail(email);
        letterService.sendLetterToEmail(Collections.singletonList(letter));
        //设置默认余额
        user.setMoney(100L);
        userMapper.insert(user);
        msg = message("user.register.success");

        return msg;
    }

    @Override
    public void resetPasswordByEmail(UserResetPasswordDTO userResetPasswordDTO) {
        //检验验证码是否正确
        //从redis中获取验证码
        // 根据用户名查询用户信息
        if (!AccountValidator.isValidEmail(userResetPasswordDTO.getUsername())) {
            //用户名不合法
            throw new UserNotExistsException();
        }
        if (userResetPasswordDTO.getPassword().length() < UserConstants.PASSWORD_MIN_LENGTH
                || userResetPasswordDTO.getPassword().length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new UserException(message("user.password.length"));
        } else if (!userResetPasswordDTO.getPassword().equals(userResetPasswordDTO.getConfirmPassword())) {
            throw new UserConfirmPasswordNotEqualsException();
        }
        //将邮箱转换为小写
        userResetPasswordDTO.setUsername(userResetPasswordDTO.getUsername().toLowerCase());

        String verify = message("user.reset.password.prefix") + userResetPasswordDTO.getUsername();
        String code = (String) redisTemplate.opsForValue().get(verify);
        if (code == null) {
            //验证码已过期
            throw new CodeTimeoutException();
        } else if (!code.equals(userResetPasswordDTO.getCode())) {
            //验证码不匹配
            throw new CodeErrorException();
        }
        if (StringUtils.isEmpty(userResetPasswordDTO.getPassword())) {
            //密码为空
            throw new UserPasswordNotMatchException();
        }

        //删除验证码
        redisTemplate.delete(verify);
        //为了方便查询，将用户名和密码封装到User对象中
        //对前端传过来的明文密码进行MD5加密处理
        User user = User.builder().password(DigestUtils.md5DigestAsHex(userResetPasswordDTO.getPassword().getBytes())).email(userResetPasswordDTO.getUsername()).build();

        userMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()));

    }

    @Override
    public UserVO getUserInfo(Long userId) {
        //初始化每日任务
        LocalDateTime now = LocalDateTime.now();
        //用户存储在redis中的task的key的格式为 task:userId:日期
        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        //查询redis 判断当redis中是否存有该用户的key
        String jsonStr;
        Boolean hasKey = stringRedisTemplate.hasKey(userKey);
        if (!Boolean.TRUE.equals(hasKey)) {
            //如果不存在 查询redis中的task:taskDetails 并给当前用户赋值该taskDestails里面的内容
            //根据task:taskDetails获取所有任务
            jsonStr = stringRedisTemplate.opsForValue().get("task:taskDetails");
            //并且需要在redis中添加一个key为task:userId:日期
            stringRedisTemplate.opsForValue().set(userKey, jsonStr);
        }


        UserVO userVO = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(CACHE_USER_INFO_KEY + userId), UserVO.class);
        if (userVO == null || userVO.getId() == null) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new UserNotExistsException();
            }
            userVO = BeanUtil.copyProperties(user, UserVO.class);
            stringRedisTemplate.opsForValue().set(CACHE_USER_INFO_KEY + userId, JSONUtil.toJsonStr(userVO), Duration.ofHours(12));
        }
        return userVO;
    }

    @Override
    public ConcurrentHashMap<String, List> getUserRepository(Long userId) {

        ConcurrentHashMap<String, List> repository = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(CACHE_USER_REPOSITORY_KEY + userId), ConcurrentHashMap.class);

        if (CollUtil.isEmpty(repository)) {
            repository = new ConcurrentHashMap<>();
            User user = userMapper.selectById(userId);

            if (user == null) {
                throw new UserNotExistsException();
            }
            List<FontVO> fonts = user.getFonts();
            repository.put("fonts", CollUtil.isEmpty(fonts) ? Collections.emptyList() : fonts);

            List<PaperVO> papers = user.getPapers();
            repository.put("papers", CollUtil.isEmpty(papers) ? Collections.emptyList() : papers);

            List<FontColorVO> fontColors = user.getFontColors();
            repository.put("fontColors", CollUtil.isEmpty(fontColors) ? Collections.emptyList() : fontColors);

            List<SignetVO> signets = user.getSignets();
            repository.put("signets", CollUtil.isEmpty(signets) ? Collections.emptyList() : signets);
            stringRedisTemplate.opsForValue().set(CACHE_USER_REPOSITORY_KEY + userId, JSONUtil.toJsonStr(repository), Duration.ofHours(24));
        }

        return repository;
    }

    @Override
    public void updateUsername(UserUpdateDTO userUpdateDTO) {
        //管理员用户名禁止更改
        Long userId = UserContext.getUserId();
        if (userId == 1L) {
            throw new UserException(MessageUtils.message("user.admin.error"));
        }
        //检验用户名是否合法
        if (StringUtils.isEmpty(userUpdateDTO.getUsername()) || !AccountValidator.isValidUsername(userUpdateDTO.getUsername())) {
            throw new UserException(message("user.username.length"));
        }
        //检验用户名是否存在
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userUpdateDTO.getUsername())) != null) {
            throw new UserException(message("user.username.exists"));
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }
        user.setUsername(userUpdateDTO.getUsername());
        userMapper.updateById(user);
        stringRedisTemplate.delete(CACHE_USER_INFO_KEY + userId);
    }

    @Override
    public void updatePassword(UserUpdateDTO userUpdateDTO) {
        //检验密码是否合法
        if (StringUtils.isEmpty(userUpdateDTO.getOldPassword()) || StringUtils.isEmpty(userUpdateDTO.getNewPassword()) || StringUtils.isEmpty(userUpdateDTO.getConfirmPassword())) {
            throw new UserPasswordNotMatchException();
        }
        //检验新密码是否合法
        if (userUpdateDTO.getNewPassword().length() < UserConstants.PASSWORD_MIN_LENGTH
                || userUpdateDTO.getNewPassword().length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new UserException("user.password.length", null);
        } else if (!userUpdateDTO.getNewPassword().equals(userUpdateDTO.getConfirmPassword())) {
            throw new UserConfirmPasswordNotEqualsException();
        }

        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        //检验旧密码是否正确
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(userUpdateDTO.getOldPassword().getBytes()))) {
            throw new UserException("user.old.password.error", null);
        }

        user.setPassword(DigestUtils.md5DigestAsHex(userUpdateDTO.getNewPassword().getBytes()));
        userMapper.updateById(user);
    }

    @Override
    public void updateUserInfo(UserUpdateDTO userUpdateDTO) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }
        if (StringUtils.isNotEmpty(userUpdateDTO.getNickname())) {
            user.setNickname(userUpdateDTO.getNickname());
        }
        if (userUpdateDTO.getAvatarId() != null) {
            Avatar avatar = avatarMapper.selectById(userUpdateDTO.getAvatarId());
            user.setAvatar(avatar.getUrl());
        }
        if (StringUtils.isNotEmpty(userUpdateDTO.getSex())) {
            user.setSex(userUpdateDTO.getSex());
        }
        userMapper.updateById(user);
        stringRedisTemplate.delete(CACHE_USER_INFO_KEY + userId);

    }

    @Override
    public Long getUserMoney(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }

        //在这里获取当前线程用户的id 根据此id设置用于redis key
        LocalDateTime now = LocalDateTime.now();
        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String jsonStr = stringRedisTemplate.opsForValue().get(userKey);
        if (StringUtils.isEmpty(jsonStr)){
            getTask(userId);
        }
        // 强耦合一下，懒得改了，能用着先
        return user.getMoney();
    }

    @Override
    public List<FriendVO> getMyFriends(Long userId) {
        //查询好友列表, 从 Redis 中获取好友列表 , 如果不存在则查询数据库 , 并将查询结果存入 Redis, 作用是减少数据库查询次数
        List<Friend> friendList = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_USER_FRIENDS_KEY + userId), Friend.class);
        if (CollUtil.isEmpty(friendList)) {
            friendList = friendMapper.selectList(new LambdaQueryWrapper<Friend>().eq(Friend::getOwningId, userId));
            if (CollUtil.isEmpty(friendList)) {
                return Collections.emptyList();
            }
        }


        // 可能会出现好友信息为空的情况，需要处理，但如果好友信息为空，说明该用户并不经常访问，更不会更新自己的个人信息，直接使用旧数据即可，不需要再次查询数据库
        List<UserVO> friends = new ArrayList<>(Objects.requireNonNull(stringRedisTemplate.opsForValue().multiGet(friendList.stream()
                        .map(Friend::getUserId)
                        .map(id -> CACHE_USER_INFO_KEY + id)
                        .collect(Collectors.toList())))
                .stream()
                .filter(Objects::nonNull)
                .map(json -> JSONUtil.toBean(json, UserVO.class))
                .toList());
        // 查询信息为空，说明缓存中没有数据，需要查询数据库
        if (CollUtil.hasNull(friends) || friends.size() != friendList.size()) {
            List<Long> missingFriendIds = friendList.stream()
                    .map(Friend::getUserId)
                    .filter(id -> friends.stream().noneMatch(friend -> friend.getId().equals(id)))
                    .collect(Collectors.toList());
            if (!missingFriendIds.isEmpty()) {
                List<User> missingUsers = userMapper.selectBatchIds(missingFriendIds);
                List<UserVO> missingFriends = BeanUtil.copyToList(missingUsers, UserVO.class);
                friends.addAll(missingFriends);
                for (UserVO userVO : missingFriends) {
                    if (userVO != null) {
                        stringRedisTemplate.opsForValue().set(CACHE_USER_INFO_KEY + userVO.getId(), JSONUtil.toJsonStr(userVO), Duration.ofHours(12));
                    }
                }
            }
        }

        // 将 friends 转换为 Map，以便快速查找，更新好友信息模块
        Map<Long, UserVO> userVOMap = friends.stream()
                .collect(Collectors.toMap(UserVO::getId, userVO -> userVO, (existing, replacement) -> existing));
        for (Friend friend : friendList) {
            UserVO userVO = userVOMap.get(friend.getUserId());
            if (userVO != null) {
                friend.setName(userVO.getNickname());
                friend.setSex(userVO.getSex());
                friend.setEmail(userVO.getEmail());
                friend.setAvatar(userVO.getAvatar());
            }
        }
        stringRedisTemplate.opsForValue().set(CACHE_USER_FRIENDS_KEY + userId, JSONUtil.toJsonStr(friendList), Duration.ofHours(12));
        // 这个更新虽然非必要更新，但是为了保证数据的一致性，还是更新一下
        friendMapper.updateById(friendList);
        return BeanUtil.copyToList(friendList, FriendVO.class);
    }

    @Override
    public List<Address> getMyAddress(Long userId) {
        List<Address> list = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_USER_ADDRESSES_KEY + userId), Address.class);
        if (CollUtil.isEmpty(list)) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new UserNotExistsException();
            }
            list = user.getAddresses();
            stringRedisTemplate.opsForValue().set(CACHE_USER_ADDRESSES_KEY + userId, JSONUtil.toJsonStr(list), Duration.ofHours(12));
        }
        return list;
    }

    // 接口已弃用
    @Override
    public List<Address> getFriendAddress(Long friendId) {
        //查询好友地址,根据好友id和所属用户id查询
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>().eq(Friend::getId, friendId).eq(Friend::getOwningId, UserContext.getUserId()));
        if (friend == null) {
            throw new FriendNotExistsException();
        }
        return friend.getAddresses();
    }

    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String sender;
    @Value("${spring.mail.nickname}")
    private String nickname;

    @Override
    public void sendResetPasswordCode(String email) {
        email = email.toLowerCase();

        //判断邮箱是否合法
        if (!AccountValidator.isValidEmail(email)) {
            throw new BaseException(message("email.format.error"));
        }
        String verify = message("user.reset.password.prefix") + email;
        //判断5分钟内是否发送过验证码
        if (redisTemplate.hasKey(verify)) {
            throw new UserException(message("user.get.code.limit"));
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        // 验证邮箱是否未注册
        if (user == null) {
            throw new BaseException(message("email.not.exists"));
        }

        // 创建一个邮件
        //SimpleMailMessage message = new SimpleMailMessage();
        // 创建一个 MimeMessage 代替 SimpleMailMessage
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 设置发件人
            helper.setFrom(nickname + '<' + sender + '>');

            // 设置收件人
            helper.setTo(email);

            // 设置邮件主题
            helper.setSubject(nickname + "-重置密码");

            // 生成六位随机数
            String code = RandomUtil.randomNumbers(6);
            log.info("重置密码验证码：{}", code);

            // 将验证码存入 redis，有效期为5分钟
            redisTemplate.opsForValue().set(verify, code, Duration.ofMinutes(5));

            // 定义邮件内容，使用 HTML
            String content = "<div style='font-family: Arial, sans-serif;'>" +
                    "<h1>" + nickname + "账户密码重置 </h1>" +
                    "<h2>你好，" + user.getNickname() + "<h2>" +
                    "<h2>【验证码】您的重置密码验证码为：" + code + "</h2>" +
                    "<p style='font-size: 14px;'>请在五分钟内使用此验证码重置您的密码，逾期作废。</p>" +
                    "<p style='font-size: 14px;'>如果您没有请求重置密码，请忽略此邮件。</p>" +
                    "<hr>" +
                    "<p style='font-size: 12px; color: gray;'>此邮件为系统自动发送，请勿回复。</p>" +
                    "</div>";

            // 设置邮件内容为 HTML
            helper.setText(content, true);

            // 发送邮件
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new UserException(message("user.sent.code.failed"));
        } catch (MailException e) {
            throw new UserException(message("user.sent.code.failed.by.email"));
        }
    }

    @Override
    public Map<String, String> getCode() {
        //设置验证码的宽和高，获取验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 30);

        //设置验证码的唯一标识uuid
        String verify = IdUtil.simpleUUID();

        //图形验证码写出，可以写出到文件，也可以写出到流
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        captcha.write(os);
        //获取验证码
        String code = captcha.getCode();
        log.info("获取验证码:{}", code);

        //将验证码存入redis
        redisTemplate.opsForValue().set(verify, code, Duration.ofMinutes(5));

        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);

        map.put("uuid", verify);
        map.put("img", Base64.encode(os.toByteArray()));
        return map;
    }

    @Override
    public void sendCode(String email) {
        // 邮箱转小写
        email = email.toLowerCase();
        // 验证邮箱是否已经注册
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, email))) {
            throw new UserException(message("email.exists"));
        }
        //判断邮箱是否合法
        else if (!AccountValidator.isValidEmail(email)) {
            throw new UserException(message("email.format.error"));
        }

        String verify = message("user.register.prefix") + email;
        //判断5分钟内是否发送过验证码
        if (redisTemplate.hasKey(verify)) {
            throw new UserException(message("user.sent.code.limit"));
        }

        // 创建一个 MimeMessage 代替 SimpleMailMessage
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 设置发件人
            helper.setFrom(nickname + '<' + sender + '>');

            // 设置收件人
            helper.setTo(email);

            // 设置邮件主题
            helper.setSubject("欢迎访问 " + nickname);

            // 生成六位随机数
            String code = RandomUtil.randomNumbers(6);
            log.info("邮箱验证码：{}", code);

            // 将验证码存入 redis，有效期为5分钟
            redisTemplate.opsForValue().set(verify, code, Duration.ofMinutes(5));

            // 定义邮件内容，使用 HTML
            String content = "<div style='font-family: Arial, sans-serif;'>" +
                    "<h1>欢迎访问 " + nickname + "</h1>" +
                    "<h2>【验证码】您的验证码为：" + code + "</h2>" +
                    "<p style='font-size: 14px;'>验证码五分钟内有效，逾期作废。</p>" +
                    "<hr>" +
                    "<p style='font-size: 12px; color: gray;'>此邮件为系统自动发送，请勿回复。</p>" +
                    "</div>";

            // 设置邮件内容为 HTML
            helper.setText(content, true);

            // 发送邮件
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new UserException(message("user.sent.code.failed"));
        } catch (MailException e) {
            throw new UserException(message("user.sent.code.failed.by.email"));
        }

    }

    @Override
    public List<FunctionCardVO> getMyFunctionCard(Long userId) {
        // 从 Redis 中获取功能卡列表 , 如果不存在则查询数据库 , 并将查询结果存入 Redis, 作用是减少数据库查询次数
        List<FunctionCardVO> functionCardVOS = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_USER_FUNCTION_CARDS_KEY + userId), FunctionCardVO.class);
        if (CollUtil.isEmpty(functionCardVOS)) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new UserNotExistsException();
            }
            functionCardVOS = user.getFunctionCards();
            stringRedisTemplate.opsForValue().set(CACHE_USER_FUNCTION_CARDS_KEY + userId, JSONUtil.toJsonStr(functionCardVOS), Duration.ofHours(12));
        }
        return functionCardVOS;
    }

    @Override
    public List<Avatar> getAvatarList() {
        // 从 Redis 中获取头像列表 , 如果不存在则查询数据库 , 并将查询结果存入 Redis, 作用是减少数据库查询次数
        List<Avatar> avatars = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_AVATARS_KEY), Avatar.class);
        if (avatars == null || avatars.isEmpty()) {
            avatars = avatarMapper.selectList(null);
            stringRedisTemplate.opsForValue().set(CACHE_AVATARS_KEY, JSONUtil.toJsonStr(avatars), Duration.ofHours(24));
        }
        return avatarMapper.selectList(null);
    }

    @Override
    public List<Country> getCountries() {
        ;
        // 从 Redis 中获取国家列表 , 如果不存在则查询数据库 , 并将查询结果存入 Redis, 作用是减少数据库查询次数
        List<Country> countries = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_COUNTRIES_KEY), Country.class);
        if (countries == null || countries.isEmpty()) {
            countries = countryMapper.selectList(null);
            stringRedisTemplate.opsForValue().set(CACHE_COUNTRIES_KEY, JSONUtil.toJsonStr(countries), Duration.ofHours(24));
        }
        return countries;
    }

    @Override
    public void setUserDefaultAddress(Long addressId) {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        List<Address> addresses = user.getAddresses();
        // 检查 addressId 是否存在于 addresses 中
        boolean addressIdExists = addresses.stream()
                .anyMatch(address -> address.getId().equals(addressId));

        if (!addressIdExists) {
            // 处理 addressId 不存在的情况，例如抛出异常或返回错误信息
            throw new UserException(message("user.address.not.exists"));
        }
        for (Address address : addresses) {
            if (address.getId().equals(addressId)) {
                address.setIsDefault(String.valueOf(true));
            } else {
                address.setIsDefault(String.valueOf(false));
            }
        }
        user.setAddresses(addresses);
        userMapper.updateById(user);
        stringRedisTemplate.delete(CACHE_USER_ADDRESSES_KEY + UserContext.getUserId());
    }

    @Override
    public void deleteUserAddress(Long addressId) {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        List<Address> addresses = user.getAddresses();
        // 将 addresses 转换为 Map，以便快速查找
        Map<Long, Address> addressMap = addresses.stream()
                .collect(Collectors.toMap(Address::getId, address -> address));

        // 检查 addressId 是否存在于 addresses 中
        if (!addressMap.containsKey(addressId)) {
            // 处理 addressId 不存在的情况，例如抛出异常或返回错误信息
            throw new UserException(message("user.address.not.exists"));
        }

        // 检查地址是否为默认地址
        if ("true".equals(addressMap.get(addressId).getIsDefault())) {
            // 处理默认地址禁止删除的情况，例如抛出异常或返回错误信息
            throw new UserException(message("user.address.default.not.delete"));
        }
        addresses.removeIf(address -> address.getId().equals(addressId));
        user.setAddresses(addresses);
        userMapper.updateById(user);
        stringRedisTemplate.delete(CACHE_USER_ADDRESSES_KEY + UserContext.getUserId());
    }

    @Override
    public void setFriendDefaultAddress(Long friendId, Long addressId) {
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>().eq(Friend::getId, friendId).eq(Friend::getOwningId, UserContext.getUserId()));
        if (friend == null) {
            throw new FriendNotExistsException();
        }
        List<Address> addresses = friend.getAddresses();
        // 检查 addressId 是否存在于 addresses 中
        boolean addressIdExists = addresses.stream()
                .anyMatch(address -> address.getId().equals(addressId));

        if (!addressIdExists) {
            // 处理 addressId 不存在的情况，例如抛出异常或返回错误信息
            throw new UserException(message("user.address.not.exists"));
        }

        for (Address address : addresses) {
            if (address.getId().equals(addressId)) {
                address.setIsDefault(String.valueOf(true));
            } else {
                address.setIsDefault(String.valueOf(false));
            }
        }
        friend.setAddresses(addresses);
        friendMapper.updateById(friend);
        stringRedisTemplate.delete(CACHE_USER_FRIENDS_KEY + UserContext.getUserId());
    }

    @Override
    public void deleteFriendAddress(Long friendId, Long addressId) {
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>().eq(Friend::getId, friendId).eq(Friend::getOwningId, UserContext.getUserId()));
        if (friend == null) {
            throw new FriendNotExistsException();
        }
        List<Address> addresses = friend.getAddresses();
        // 将 addresses 转换为 Map，以便快速查找
        Map<Long, Address> addressMap = addresses.stream()
                .collect(Collectors.toMap(Address::getId, address -> address));

        // 检查 addressId 是否存在于 addresses 中
        if (!addressMap.containsKey(addressId)) {
            // 处理 addressId 不存在的情况，例如抛出异常或返回错误信息
            throw new FriendException(message("friend.address.not.exists"));
        }

        // 检查地址是否为默认地址
        if ("true".equals(addressMap.get(addressId).getIsDefault())) {
            // 处理默认地址禁止删除的情况，例如抛出异常或返回错误信息
            throw new FriendException(message("friend.address.default.not.delete"));
        }
        addresses.removeIf(address -> address.getId().equals(addressId));
        friend.setAddresses(addresses);
        friendMapper.updateById(friend);
        stringRedisTemplate.delete(CACHE_USER_FRIENDS_KEY + UserContext.getUserId());
    }

    @Override
    public void updateFriendRemark(Long friendId, String remark) {
        Friend friend = friendMapper.selectById(friendId);
        if (friend == null) {
            throw new FriendNotExistsException();
        }
        friend.setRemark(remark);
        friendMapper.updateById(friend);
        stringRedisTemplate.delete(CACHE_USER_FRIENDS_KEY + UserContext.getUserId());
    }

    @Override
    public void sign(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        // 获取当前签到的key
        String prefix = stringRedisTemplate.opsForValue().get(SIGN_CURRENT_KEY);
        String key = SIGN_PREFIX_KEY + prefix + SIGN_SUFFIX_KEY + userId;
        // 写入Redis SETBIT key offset 1
        // 检查用户今天是否已经签到
        Boolean isSigned = stringRedisTemplate.opsForValue().getBit(key, now.getDayOfWeek().getValue() - 1);
        if (Boolean.TRUE.equals(isSigned)) {
            throw new UserException(message("user.sign.today"));
        }
        // 更新Redis中的签到状态
        stringRedisTemplate.opsForValue().setBit(key, now.getDayOfWeek().getValue()-1, true);
        // 删除当天的签到签到缓存
        stringRedisTemplate.delete(SIGN_TODAY_KEY + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ":" + userId);
        // 获取已签到的天数
        int signedDays = getSignedDays(now, key);

        List<UserSignAward> signAwardList = JSONUtil.toList(stringRedisTemplate.opsForValue().get(SIGN_AWARD_KEY+prefix), UserSignAward.class);
        UserSignAward signAward = signAwardList.stream().filter(userSignAward -> userSignAward.getSignDays() == signedDays).findFirst().orElse(null);
        if (signAward == null) {
            throw new UserException(message("user.sign.award.error"));
        }
        //TODO 获取签到奖励
        switch (signAward.getAwardType())
        {
            case 1:
                // 猪仔钱
                User user = userMapper.selectById(userId);
                user.setMoney(user.getMoney() + signAward.getAwardNum());
                userMapper.updateById(user);
                sendSignSuccessMessage(userId,message("user.sign.award.money"));
                break;
            case 2:
                signAwardCard(userId,signAward.getAwardId());
                sendSignSuccessMessage(userId,message("user.sign.award.card"));
                // 功能卡
                break;
            case 3:
                boolean isAdd = signAwardFont(userId);
                if (isAdd) {
                    // 字体
                    sendSignSuccessMessage(userId,message("user.sign.award.font"));
                }else {
                    // 猪仔钱
                    User user2 = userMapper.selectById(userId);
                    user2.setMoney(user2.getMoney() + 100);
                    userMapper.updateById(user2);
                    sendSignSuccessMessage(userId,message("user.sign.award.font.full.money"));
                }
                // 字体
                break;
            case 4:
                // 字体颜色
                break;
            case 5:
                // 纸张
                break;
            case 6:
                // 其他收藏品
                break;
            case 7:
                // 头像
                break;
            case 8:
                // xx
                break;
            default:
                throw new UserException(message("user.sign.award.error"));
        }
    }

    @Override
    public List<TaskTable> getTask(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        //用户存储在redis中的task的key的格式为 task:userId:日期
        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //查询redis 判断当redis中是否存有该用户的key
        String jsonStr;
        Boolean hasKey = stringRedisTemplate.hasKey(userKey);

        if (!Boolean.TRUE.equals(hasKey)) {
            //如果不存在 查询redis中的task:taskDetails 并给当前用户赋值该taskDestails里面的内容
            //根据task:taskDetails获取所有任务
            jsonStr = stringRedisTemplate.opsForValue().get("task:taskDetails");
            //并且需要在redis中添加一个key为task:userId:日期
            stringRedisTemplate.opsForValue().set(userKey, jsonStr);
        } else {
            //如果存在 就直接查询该用户的key
            //根据userKey查询 获得其所有的对象
            jsonStr = stringRedisTemplate.opsForValue().get(userKey);
        }

        try {
            // 将json字符串转换为TaskTable对象列表
            List<TaskTable> taskTableList = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, TaskTable.class));
            return taskTableList;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }


    //完成任务 领取奖励
    @Override
    public void finishTask(Long taskId) {
        Long userId = UserContext.getUserId();

        //在这里获取当前线程用户的id 根据此id设置用于redis key
        LocalDateTime now = LocalDateTime.now();
        int money;

        String userKey = "task" + ":" +  userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String jsonStr = stringRedisTemplate.opsForValue().get(userKey);
        //通过这个键 获取导redis中的值 并且将这个值转化为TaskTable对象的集合
        try {
            // 将json字符串转换为TaskTable对象列表
            List<TaskTable> taskTableList = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, TaskTable.class));
            //获取taskTableList中id为taskId的对象
            TaskTable taskTable = taskTableList.stream().filter(t -> t.getId().equals(taskId)).findFirst().orElse(null);
            //将对象中的 status 设为1
            taskTable.setStatus(2);
            money = taskTable.getMoney();
            //将修改后的对象重新转换为json字符串并存入redis
            stringRedisTemplate.opsForValue().set(userKey, objectMapper.writeValueAsString(taskTableList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        //根据userId更改用户表中的money字段 将用户表中的money字段加money
        User user = userMapper.selectById(userId);
        user.setMoney(user.getMoney() + money);
        userMapper.updateById(user);

    }



    void sendSignSuccessMessage(Long userId,String message){
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        map.put("userId", userId);
        map.put("message", message);
        rabbitTemplate.convertAndSend(EXCHANGE_AI_DIRECT, ROUTING_KEY_SIGN_AWARD, map);
    }
    // 没空管这个了，先写成💩吧
    public boolean signAwardFont(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }
        List<FontVO> fonts = user.getFonts();
        if (CollUtil.isEmpty(fonts)) {
            throw new UserNotExistsException();
        }
        List<FontVO> fontList = BeanUtil.copyToList(fontMapper.selectList(null), FontVO.class);
        fontList.removeAll(fonts);
        if (!fontList.isEmpty()) {
            FontVO randomFont = fontList.get(RandomUtil.randomInt(fontList.size()));
            fonts.add(randomFont);
        }else {
            return false;
        }

        user.setFonts(fonts);
        userMapper.updateById(user);
        stringRedisTemplate.delete(CACHE_USER_REPOSITORY_KEY + userId);
        return true;
    }
    public void signAwardCard(Long userId,Long cardId) {
        FunctionCard functionCard = cardMapper.selectById(cardId);
        if (functionCard == null) {
            throw new CardException(message("card.not.exists"));
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }
        List<FunctionCardVO> userFunctionCards = user.getFunctionCards();
        if (userFunctionCards == null) {
            userFunctionCards = Collections.emptyList();
        }
        boolean hasCard = false;
        for (FunctionCardVO functionCardVO : userFunctionCards) {
            if (functionCardVO.getId().equals(cardId)) {
                functionCardVO.setNumber(functionCardVO.getNumber() + 1);
                hasCard = true;
                break;
            }
        }
        if (!hasCard) {
            FunctionCardVO functionCardVO = BeanUtil.copyProperties(functionCard, FunctionCardVO.class);
            functionCardVO.setNumber(1);
            userFunctionCards.add(functionCardVO);
        }else {
            user.setFunctionCards(userFunctionCards);
        }
        userMapper.updateById(user);
        stringRedisTemplate.delete(CACHE_USER_FUNCTION_CARDS_KEY + userId);
    }
    @Override
    public ConcurrentHashMap<String,Object> getSignList(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        String todayKey = SIGN_TODAY_KEY + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ":" + userId;
        ConcurrentHashMap userSignAwardsMap = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(todayKey),ConcurrentHashMap.class);
        if (CollUtil.isEmpty(userSignAwardsMap)) {
            // 获取当前签到的key
            String prefix = stringRedisTemplate.opsForValue().get(SIGN_CURRENT_KEY);
            String key = SIGN_PREFIX_KEY + prefix + SIGN_SUFFIX_KEY + userId;
            // 获取已签到的天数
            int signedDays = getSignedDays(now, key);
            // 获取当前周期签到奖励
            List<UserSignAward> signAwardList = JSONUtil.toList(stringRedisTemplate.opsForValue().get(SIGN_AWARD_KEY+prefix), UserSignAward.class);
            List<UserSignAwardVO >userSignAwards = BeanUtil.copyToList(signAwardList, UserSignAwardVO.class);
            userSignAwards.forEach(userSignAward -> {
                if (userSignAward.getSignDays() <= signedDays) {
                    userSignAward.setReceived(true);
                }
            });
            userSignAwardsMap.put("signedDays", signedDays);
            userSignAwardsMap.put("userSignAwards", userSignAwards);
            userSignAwardsMap.put("isSignToday", Objects.requireNonNull(stringRedisTemplate.opsForValue().getBit(key, now.getDayOfWeek().getValue() - 1)));
            stringRedisTemplate.opsForValue().set(todayKey, JSONUtil.toJsonStr(userSignAwardsMap), Duration.ofDays(1));
        }

        return userSignAwardsMap;
    }

    @Override
    public UserStatistics getUserStatistics(Long userId) {
        List<Friend> friendList = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_USER_FRIENDS_KEY + userId), Friend.class);
        int friendCount = 0;
        if (CollUtil.isEmpty(friendList)) {
            friendCount = getMyFriends(userId).size();
        }else {
            friendCount = friendList.size();
        }
        Set<Long> letterIds = JSON.parseObject(stringRedisTemplate.opsForValue().get(CACHE_USER_RECEIVE_LETTER_KEY + userId), Set.class);
        int receiveLetterCount = 0;
        if (CollUtil.isEmpty(letterIds)) {
            receiveLetterCount = letterService.getMyReceiveLetter(userId).size();
        }else {
            receiveLetterCount = letterIds.size();
        }
        return UserStatistics.builder()
                .friendCount(friendCount)
                .receiveLetterCount(receiveLetterCount)
                .build();
    }



    private int getSignedDays(LocalDateTime now,String key ) {
        // 保证和调用方法时刻、用户一致
        // 1.获取今天是本周的第几天
        int dayOfWeek = now.getDayOfWeek().getValue();
        // 2.获取本月截止今天为止的所有的签到记录，返回的是一个十进制的数字 BITFIELD sign:5:202203 GET u14 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfWeek)).valueAt(0)
        );
        if (result == null || result.isEmpty()) {
            // 没有任何签到结果
            return 0;
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return 0;
        }
        // 3.循环遍历
        int count = 0;
        while (num != 0) {
            // 让这个数字与1做与运算，得到数字的最后一个bit位，判断这个bit位是否为1
            if ((num & 1) == 1) {
                // 如果为1，计数器+1
                count++;
            }
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }
        return count;
    }
    //生成随机用户名，数字和字母组成,
    public String getStringRandom(int length) {

        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (random.nextInt(26) + temp);
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }
}

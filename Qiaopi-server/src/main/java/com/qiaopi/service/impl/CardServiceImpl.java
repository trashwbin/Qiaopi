package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.qiaopi.constant.LetterConstants;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.FunctionCardUseDTO;
import com.qiaopi.entity.FunctionCard;
import com.qiaopi.entity.Letter;
import com.qiaopi.entity.User;
import com.qiaopi.exception.base.BaseException;
import com.qiaopi.exception.card.CardException;
import com.qiaopi.exception.letter.LetterException;
import com.qiaopi.exception.user.UserException;
import com.qiaopi.exception.user.UserNotExistsException;
import com.qiaopi.mapper.CardMapper;
import com.qiaopi.mapper.LetterMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.CardService;
import com.qiaopi.service.LetterService;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.ProgressUtils;
import com.qiaopi.vo.FunctionCardShopVO;
import com.qiaopi.vo.FunctionCardVO;
import com.qiaopi.vo.LetterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.qiaopi.constant.CacheConstant.*;
import static com.qiaopi.utils.MessageUtils.message;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;
    private final LetterMapper letterMapper;
    private final UserMapper userMapper;
    private final LetterService letterService;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;

    @Override
    @Transactional
    public LetterVO useCard(FunctionCardUseDTO functionCardUseDTO) {
        Long userId = UserContext.getUserId();

        // 1. 根据功能卡id查询功能卡信息
        FunctionCard functionCard = cardMapper.selectById(functionCardUseDTO.getCardId());
        if (functionCard == null) {
            throw new CardException(message("card.not.exists"));
        }
        // 2. 根据letter信息查询对应的信件信息
        Letter letter = letterMapper.selectById(functionCardUseDTO.getLetterId());
        if (letter == null) {
            throw new LetterException(message("letter.not.exists"));
        } else if (letter.getStatus() == LetterConstants.DELIVERED) {
            throw new LetterException(message("letter.is.delivered"));
        }

        // 3. 根据信件信息查询对应的用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(message("user.not.exists"));
        }
        //检查用户是否有此功能卡
        List<FunctionCardVO> userFunctionCards = user.getFunctionCards();
        boolean hasCard = true;
        if (userFunctionCards == null || userFunctionCards.isEmpty()) {
            hasCard = false;
        } else {
            // 空值检查
            if (functionCardUseDTO == null || functionCardUseDTO.getCardId() == null) {
                throw new IllegalArgumentException("functionCardUseDTO or cardId is null");
            }

            // 遍历列表并更新符合条件的对象的 number 属性，若减为 0 则删除该对象
            for (Iterator<FunctionCardVO> iterator = userFunctionCards.iterator(); iterator.hasNext(); ) {
                FunctionCardVO functionCardVO = iterator.next();
                if (functionCardVO.getId().equals(functionCardUseDTO.getCardId()) && functionCardVO.getNumber() > 0) {
                    functionCardVO.setNumber(functionCardVO.getNumber() - 1);
                    if (functionCardVO.getNumber() == 0) {
                        iterator.remove(); // 删除 number 减为 0 的对象
                    }
                    break; // 找到并更新后退出循环
                }
            }


        }
        if (!hasCard) {
            throw new UserException(message("user.card.not.exists"));
        }

        // 4. 更新用户信息
        user.setFunctionCards(userFunctionCards);
        userMapper.updateById(user);
        stringRedisTemplate.delete(CACHE_USER_FUNCTION_CARDS_KEY + userId);
        stringRedisTemplate.delete(CACHE_USER_WRITE_LETTER_KEY + userId);
        boolean isDelivery = false;
        // 5. 更新信件信息
        if (functionCard.getCardType() == 1) {
            //加速卡
            letter.setSpeedRate(functionCard.getSpeedRate());
            ProgressUtils.getProgress(letter);
        }

        if (functionCard.getCardType() == 2) {

            // 减时卡
            if (functionCard.getId() == 0){
                letter.setDeliveryProgress(10000L);
                letter.setReduceTime("-1");
            }else {
                letter.setReduceTime(String.valueOf(Integer.parseInt(letter.getReduceTime())+Integer.parseInt(functionCard.getReduceTime())));
                ProgressUtils.getProgress(letter);
            }
        }
        if (letter.getDeliveryProgress() >= 10000) {
            isDelivery = true;
        }
        letterMapper.updateById(letter);
        // 调整剩余时间
        if (isDelivery) {
            // 异步发送信件
            CompletableFuture.runAsync(() -> letterService.sendLetterToEmail(Collections.singletonList(letter)));
            //letterService.sendLetterToEmail(Collections.singletonList(letter));
        }
        //6 更新信件状态
        return BeanUtil.copyProperties(letter, LetterVO.class);
    }

    @Override
    public List<FunctionCardShopVO> list() {
        Long userId = UserContext.getUserId();

        // 从Redis中获取功能卡列表
        List<FunctionCardShopVO> functionCardShopVOS = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_SHOP_FUNCTION_CARD_KEY), FunctionCardShopVO.class);
        if (CollUtil.isEmpty(functionCardShopVOS)) {
            functionCardShopVOS = cardMapper.selectList(null).stream().map(functionCard -> {
                FunctionCardShopVO functionCardShopVO = BeanUtil.copyProperties(functionCard, FunctionCardShopVO.class);
                functionCardShopVO.setNumber(0);
                return functionCardShopVO;
            }).toList();
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_FUNCTION_CARD_KEY, JSONUtil.toJsonStr(functionCardShopVOS), Duration.ofHours(24));
        }
        if (userId == null) {
            return functionCardShopVOS;
        }
        // 设置用户拥有功能卡数量
        List<FunctionCardVO> userFunctionCardList = JSONUtil.toList(stringRedisTemplate.opsForValue().get(CACHE_USER_FUNCTION_CARDS_KEY + userId), FunctionCardVO.class);
        if (CollUtil.isEmpty(userFunctionCardList)) {
            userFunctionCardList = userService.getMyFunctionCard(userId);
        }
        Map<Long, Integer> userFunctionCardMap = userFunctionCardList.stream().collect(Collectors.groupingBy(FunctionCardVO::getId,Collectors.summingInt(FunctionCardVO::getNumber)));
        functionCardShopVOS.forEach(functionCardShopVO -> {
            functionCardShopVO.setNumber(userFunctionCardMap.getOrDefault(functionCardShopVO.getId(), 0));
        });
        return functionCardShopVOS;
    }

    @Override
    @Transactional
    public void buyCard(Long cardId) {
        FunctionCard functionCard = cardMapper.selectById(cardId);
        if (functionCard == null) {
            throw new CardException(message("card.not.exists"));
        }
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        List<FunctionCardVO> userFunctionCards = user.getFunctionCards();
        if (userFunctionCards == null) {
            userFunctionCards = Collections.emptyList();
        }
        if (user.getMoney()<functionCard.getPrice()){
            throw new BaseException(message("user.money.not.enough"));
        }else {
            user.setMoney(user.getMoney()-functionCard.getPrice());
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
        stringRedisTemplate.delete(CACHE_USER_FUNCTION_CARDS_KEY + UserContext.getUserId());
    }


}

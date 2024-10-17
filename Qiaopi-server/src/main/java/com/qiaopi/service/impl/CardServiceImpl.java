package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.qiaopi.constant.LetterStatus;
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
import com.qiaopi.vo.FunctionCardShopVO;
import com.qiaopi.vo.FunctionCardVO;
import com.qiaopi.vo.LetterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qiaopi.utils.MessageUtils.message;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;
    private final LetterMapper letterMapper;
    private final UserMapper userMapper;
    private final LetterService letterService;

    @Override
    @Transactional
    public LetterVO useCard(FunctionCardUseDTO functionCardUseDTO) {

        // 1. 根据功能卡id查询功能卡信息
        FunctionCard functionCard = cardMapper.selectById(functionCardUseDTO.getCardId());
        if (functionCard == null) {
            throw new CardException(message("card.not.exists"));
        }
        // 2. 根据letter信息查询对应的信件信息
        Letter letter = letterMapper.selectById(functionCardUseDTO.getLetterId());
        if (letter == null) {
            throw new LetterException(message("letter.not.exists"));
        } else if (letter.getStatus() == LetterStatus.DELIVERED) {
            throw new LetterException(message("letter.is.delivered"));
        }

        // 3. 根据信件信息查询对应的用户信息
        User user = userMapper.selectById(UserContext.getUserId());
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

        // 5. 更新信件信息
        if (functionCard.getCardType() == 1) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expectedDeliveryTime = letter.getExpectedDeliveryTime();
            //加速卡
            long millis = Duration.between(now, expectedDeliveryTime).toMillis();
            double speed = Double.parseDouble(functionCard.getSpeedRate());
            // 调整剩余时间
            long adjustedMillis = (long) (millis / speed);

            // 将调整后的时间差转换为纳秒
            long adjustedNanos = Duration.ofMillis(adjustedMillis).toNanos();

            letter.setExpectedDeliveryTime(now.plusNanos(adjustedNanos));
        }

        if (functionCard.getCardType() == 2) {
            // 减时卡
            long millis = Duration.between(LocalDateTime.now(), letter.getExpectedDeliveryTime()).toMillis();
            long reduceMin = Long.parseLong(functionCard.getReduceTime()) * 60 * 1000; // 将分钟转换为毫秒
            long adjustedMillis = millis - reduceMin;

            // 调整剩余时间
            if (adjustedMillis <= 0 || functionCard.getId() == 0) {
                letter.setExpectedDeliveryTime(LocalDateTime.now());
                letterMapper.updateById(letter);
                // 发送信件
                letterService.sendLetterToEmail(Collections.singletonList(letter));
                return BeanUtil.copyProperties(letter, LetterVO.class);
            } else {
                letter.setExpectedDeliveryTime(LocalDateTime.now().plus(adjustedMillis, ChronoUnit.MILLIS));
            }
        }
        //6 更新信件状态
        letterMapper.updateById(letter);
        return BeanUtil.copyProperties(letter, LetterVO.class);
    }

    @Override
    public List<FunctionCardShopVO> list() {

        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            return cardMapper.selectList(null).stream().map(functionCard -> {
                FunctionCardShopVO functionCardShopVO = BeanUtil.copyProperties(functionCard, FunctionCardShopVO.class);
                functionCardShopVO.setNumber(0);
                return functionCardShopVO;
            }).collect(Collectors.toList());
        }
        Map<Long, Integer> userFunctionCardList = user.getFunctionCards().stream().collect(Collectors.groupingBy(FunctionCardVO::getId,Collectors.summingInt(FunctionCardVO::getNumber)));
        List<FunctionCardShopVO> functionCardShopVOS = cardMapper.selectList(null).stream().map(functionCard -> {
            FunctionCardShopVO functionCardShopVO = BeanUtil.copyProperties(functionCard, FunctionCardShopVO.class);
            functionCardShopVO.setNumber(userFunctionCardList.getOrDefault(functionCard.getId(),0));
            return functionCardShopVO;
        }).collect(Collectors.toList());
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
    }
}

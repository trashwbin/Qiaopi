package com.qiaopi.service;

import com.qiaopi.dto.FunctionCardUseDTO;
import com.qiaopi.vo.FunctionCardShopVO;
import com.qiaopi.vo.LetterVO;

import java.util.List;

public interface CardService {
    LetterVO useCard(FunctionCardUseDTO functionCardUseDTO);

    List<FunctionCardShopVO> list();

    void buyCard(Long cardId);

}

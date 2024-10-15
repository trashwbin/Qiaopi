package com.qiaopi.service;

import com.qiaopi.dto.FunctionCardUseDTO;
import com.qiaopi.vo.LetterVO;

public interface CardService {
    LetterVO useCard(FunctionCardUseDTO functionCardUseDTO);
}

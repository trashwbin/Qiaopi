package com.qiaopi.service;

import com.qiaopi.entity.Letter;
import com.qiaopi.dto.LetterSendDTO;
import com.qiaopi.vo.LetterVO;

import java.util.List;

public interface LetterService {


    /**
     * 发送信件
     */
    void sendLetterToEmail(List<Letter> letters);

    /**
     * 发送信件前封装
     *
     * @param letterSendDTO
     * @return
     */
    LetterVO sendLetterPre(LetterSendDTO letterSendDTO);

    /**
     * 获取我写的侨批
     * @return
     */
    List<LetterVO> getMySendLetter(Long userId);

    List<LetterVO> getMyReceiveLetter(Long userId);

    LetterVO getMyNotReadLetter(Long userId);

    void readLetter(Long letterId);
}





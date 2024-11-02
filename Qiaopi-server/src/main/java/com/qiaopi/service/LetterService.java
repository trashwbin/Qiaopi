package com.qiaopi.service;

import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.Letter;
import com.qiaopi.dto.LetterSendDTO;
import com.qiaopi.vo.LetterVO;
import org.springframework.http.ResponseEntity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
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
    List<LetterVO> getMySendLetter();

    List<LetterVO> getMyReceiveLetter();

    LetterVO getMyNotReadLetter();

    void readLetter(Long letterId);
}





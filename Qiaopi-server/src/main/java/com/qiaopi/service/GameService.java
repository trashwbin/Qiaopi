package com.qiaopi.service;

import com.qiaopi.entity.FontPaper;
import com.qiaopi.vo.PaperShopVO;

import java.util.List;

public interface GameService {

    void winFfl(Long userId);

    Integer getFflLimit(Long userId);
}

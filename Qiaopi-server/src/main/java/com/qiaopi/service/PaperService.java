package com.qiaopi.service;

import com.qiaopi.vo.PaperShopVO;

import java.util.List;

public interface PaperService {

    List<PaperShopVO> list();

    void buyPaper(Long paperId);
}

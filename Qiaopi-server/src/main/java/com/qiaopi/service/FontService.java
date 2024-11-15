package com.qiaopi.service;

import com.qiaopi.vo.FontColorShopVO;
import com.qiaopi.vo.FontShopVO;

import java.util.List;

public interface FontService {

    List<FontShopVO> list();

    List<FontColorShopVO> listColor();

    void buyFont(Long fontId);

    void buyFontColor(Long fontColorId);

}

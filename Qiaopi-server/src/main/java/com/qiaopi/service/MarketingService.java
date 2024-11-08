package com.qiaopi.service;

import com.qiaopi.dto.PageQueryDTO;
import com.qiaopi.vo.PageQueryVO;

public interface MarketingService {

    PageQueryVO list(PageQueryDTO pageQueryDTO);
}

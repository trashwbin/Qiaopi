package com.qiaopi.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiaopi.dto.PageQueryDTO;
import com.qiaopi.entity.Commodity;
import com.qiaopi.mapper.MarketingMapper;
import com.qiaopi.service.MarketingService;
import com.qiaopi.vo.PageQueryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class MarketingServiceImpl implements MarketingService {

    private final MarketingMapper marketingMapper;

    @Override
    public PageQueryVO list(PageQueryDTO pageQueryDTO) {
        Page<Commodity> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getLimit());
        Page<Commodity> commodityPage = marketingMapper.selectPage(page, null);
        return PageQueryVO.builder().records(commodityPage.getRecords()).total(commodityPage.getTotal()).build();
    }
}

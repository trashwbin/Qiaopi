package com.qiaopi.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.PageQueryDTO;
import com.qiaopi.entity.Commodity;
import com.qiaopi.mapper.MarketingMapper;
import com.qiaopi.service.MarketingService;
import com.qiaopi.vo.PageQueryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.qiaopi.constant.MqConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class MarketingServiceImpl implements MarketingService {

    private final MarketingMapper marketingMapper;
    private  final RabbitTemplate rabbitTemplate;
    @Override
    public PageQueryVO list(PageQueryDTO pageQueryDTO) {
        if(pageQueryDTO.getPage() == null || pageQueryDTO.getLimit() == null){
            return PageQueryVO.builder().records(Collections.emptyList()).total(0L).build();
        }
        if(pageQueryDTO.getPage() == 1 && UserContext.getUserId()!=null){
            rabbitTemplate.convertAndSend(EXCHANGE_AI_DIRECT,ROUTING_KEY_MARKETING, UserContext.getUserId());
        }
        Page<Commodity> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getLimit());
        Page<Commodity> commodityPage = marketingMapper.selectPage(page, null);
        Collections.shuffle(commodityPage.getRecords());
        return PageQueryVO.builder().records(commodityPage.getRecords()).total(commodityPage.getTotal()).build();
    }
}

package com.qiaopi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiaopi.entity.Avatar;
import com.qiaopi.entity.Commodity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MarketingMapper extends BaseMapper<Commodity> {
}

package com.qiaopi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiaopi.entity.Country;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CountryMapper extends BaseMapper<Country> {
}

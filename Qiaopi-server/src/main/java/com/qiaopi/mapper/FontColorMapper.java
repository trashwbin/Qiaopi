package com.qiaopi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiaopi.dto.LetterGenDTO;
import com.qiaopi.entity.FontColor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FontColorMapper extends BaseMapper<FontColor> {

    LetterGenDTO queryFontColor(Long id);



}

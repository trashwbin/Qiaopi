package com.qiaopi.service;

import com.qiaopi.dto.BottleGenDTO;
import com.qiaopi.vo.BottleVo;

public interface BottleService {


    /**
     * 漂流瓶生成
     * @param bottleGenDTO
     */
    String GenerateDriftBottle(BottleGenDTO bottleGenDTO);

    String  showBottle();

    BottleVo getBottle();
}

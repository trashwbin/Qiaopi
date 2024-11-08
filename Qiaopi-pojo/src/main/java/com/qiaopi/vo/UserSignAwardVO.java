package com.qiaopi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserSignAwardVO {
    // 序号
    private Long id;
    // 奖励类型
    // 1: 猪仔钱，2：功能卡，3：字体，4：字体颜色，5：纸张，6：其他收藏品
//    private Integer awardType;
    // 奖励图片
    private String previewLink;
    // 奖励描述
    private String awardDesc;
    // 奖励名称
    private String awardName;
    // 奖励数量
    private Integer awardNum;
    // 需要签到天数
    private Integer signDays;
    // 是否已领取
    private Boolean received;
}

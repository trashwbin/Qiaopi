package com.qiaopi.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class TaskTable {

    //任务序号
    private Long id;
    //任务名称
    private String taskName;
    //任务描述
    private String description;
    //任务状态 0未完成 1已完成未领取 2 已完成已领取
    private int status;
    //任务奖励
    private int money;
    //链接
    private String link;
    //路由
    private String route;

}

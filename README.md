# 侨缘信使团队

> 请勿泄露项目中的任何密钥和`IP`地址！！！
>
> 请勿泄露项目中的任何密钥和`IP`地址！！！
>
> 请勿泄露项目中的任何密钥和`IP`地址！！！
[前端项目地址](https://gitee.com/trashwbin/qiaopi_vue)
### 初始化

```mysql
ALTER TABLE letter
    ADD COLUMN piggy_money BIGINT DEFAULT 0 COMMENT '携带猪仔钱',
    ADD COLUMN letter_type INT DEFAULT 1 COMMENT '信件类型(1:竖版字体信件,2:横版信件)',
    ADD COLUMN speed_rate VARCHAR(255) DEFAULT '1' COMMENT '加速倍率',
    ADD COLUMN reduce_time VARCHAR(255) DEFAULT '0' COMMENT '减少的时间(单位:分钟)',
    ADD COLUMN delivery_time DATETIME COMMENT '送达时间';
```


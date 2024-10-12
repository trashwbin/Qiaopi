# 侨缘信使团队

> 请勿泄露项目中的任何密钥和`IP`地址！！！
>
> 请勿泄露项目中的任何密钥和`IP`地址！！！
>
> 请勿泄露项目中的任何密钥和`IP`地址！！！

### 初始化

```mysql
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    sex VARCHAR(10),
    avatar VARCHAR(255),
    password VARCHAR(255),
    status VARCHAR(10) DEFAULT '0' NOT NULL,
    del_flag VARCHAR(10) DEFAULT '0' NOT NULL,
    login_ip VARCHAR(50),
    login_date DATETIME,
    create_user BIGINT,
    create_time DATETIME,
    update_user BIGINT,
    update_time DATETIME,
    remark TEXT,
    PRIMARY KEY (id)
);

-- 插入数据时，需要为 create_user 和 update_user 提供 BIGINT 类型的值
INSERT INTO user (username, email, password, create_user, update_user) VALUES ('admin', 'Trashwbin@gmail.com', 'e10adc3949ba59abbe56e057f20f883e', 1, 1);
```

### 10.6 建表语句扩展

```mysql
-- 添加 'money' 字段
ALTER TABLE user ADD COLUMN money BIGINT DEFAULT 0;

-- 添加 'fonts' 字段，使用 JSON 格式
ALTER TABLE user ADD COLUMN fonts JSON;

-- 添加 'papers' 字段，使用 JSON 格式
ALTER TABLE user ADD COLUMN papers JSON;

INSERT INTO user (username, email, password, money, create_user, update_user, fonts, papers)
VALUES ('user0', 'admin@example.com', 'e10adc3949ba59abbe56e057f20f883e', 1000, 1, 1,
        '[{"id": 1, "name": "字体1" ,"previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}, {"id": 2, "name": "字体2","previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}]',
        '[{"id": 1, "name": "信纸1","previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}, {"id": 2, "name": "信纸2","previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}]'),
       ('user1', 'user1@example.com', 'e10adc3949ba59abbe56e057f20f883e', 500, 1, 1,
        '[{"id": 3, "name": "字体3" ,"previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}]',
        '[{"id": 3, "name": "信纸3","previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}]');

CREATE TABLE font (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(255) NOT NULL
);
CREATE TABLE paper (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL
);

-- 修改 font 表
ALTER TABLE font
    ADD COLUMN preview_image VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN file_path VARCHAR(255) NOT NULL DEFAULT '';

-- 修改 paper 表
ALTER TABLE paper
    ADD COLUMN preview_image VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN file_path VARCHAR(255) NOT NULL DEFAULT '';

-- 在 font 表中新增测试数据
INSERT INTO font (name, preview_image, file_path)
VALUES ('字体1', 'preview_font1.jpg', '/path/to/font1.ttf'),
       ('字体2', 'preview_font2.jpg', '/path/to/font2.ttf'),
       ('字体3', 'preview_font3.jpg', '/path/to/font3.ttf');

-- 在 paper 表中新增测试数据
INSERT INTO paper (name, preview_image, file_path)
VALUES ('信纸1', 'https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png', 'https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png'),
       ('信纸2', 'https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png', 'https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png'),
       ('信纸3', 'https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png', 'https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png');
```

### 10.11

```mysql
CREATE TABLE `letter` ( `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID', `sender_user_id` BIGINT COMMENT '寄件人的用户ID', `sender_name` VARCHAR(255) COMMENT '寄件人的姓名', `recipient_email` VARCHAR(255) COMMENT '收件人的邮箱', `recipient_user_id` BIGINT COMMENT '收件人的用户ID(非必需项)', `recipient_name` VARCHAR(255) COMMENT '收件人的姓名', `letter_content` TEXT COMMENT '信的内容', `letter_link` VARCHAR(255) COMMENT '信的链接', `cover_link` VARCHAR(255) COMMENT '封面链接', `sender_address` JSON COMMENT '寄件人地址', `recipient_address` JSON COMMENT '收件人地址', `expected_delivery_time` DATETIME COMMENT '预计送达时间', `status` INT COMMENT '信件状态(0:未发送 1:已发送,2:传递中,3:已送达)', `delivery_progress` BIGINT COMMENT '送信进度(0-10000)', `read_status` INT COMMENT '阅读状态(0:未读,1:已读)', `create_user` BIGINT COMMENT '创建者', `create_time` DATETIME COMMENT '创建时间', `update_user` BIGINT COMMENT '更新者', `update_time` DATETIME COMMENT '更新时间', `remark` VARCHAR(255) COMMENT '备注' ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信件表';

ALTER TABLE `paper` ADD COLUMN `recipient_translate_x` VARCHAR(255) DEFAULT NULL COMMENT '收信人x偏移量', ADD COLUMN `recipient_translate_y` VARCHAR(255) DEFAULT NULL COMMENT '收信人y偏移量', ADD COLUMN `sender_translate_x` VARCHAR(255) DEFAULT NULL COMMENT '寄信人x偏移量', ADD COLUMN `sender_translate_y` VARCHAR(255) DEFAULT NULL COMMENT '寄信人y偏移量';
```


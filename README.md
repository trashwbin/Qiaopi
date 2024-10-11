
### 建表语句

### 初始化
```
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

```
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
### 10.9
-- 添加 'addresses' 字段，使用 JSON 格式存储用户地址信息
ALTER TABLE user ADD COLUMN addresses JSON;

INSERT INTO user (username, email, password, money, create_user, update_user, fonts, papers, addresses)
VALUES ('user2', 'user2@example.com', 'e10adc3949ba59abbe56e057f20f883e', 1000, 1, 1,
        '[{"id": 1, "name": "字体1", "previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}]',
        '[{"id": 1, "name": "信纸1", "previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}]',
        '[{"id": 1, "formattedAddress": "123 Main St", "longitude": 37.7749, "latitude": -122.4194, "isDefault": "true"}]'     );

INSERT INTO user (username, email, password, money, create_user, update_user, fonts, papers, addresses)
VALUES ('user3', 'user3@example.com', 'e10adc3949ba59abbe56e057f20f883e', 500, 1, 1,
        '[{"id": 3, "name": "字体3", "previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}]',
        '[{"id": 3, "name": "信纸3", "previewImage": "https://ooo.0x0.ooo/2024/10/05/O4Xt2I.png"}]',
        '[{"id": 2, "formattedAddress": "456 Market St", "longitude": 37.7749, "latitude": -122.4194, "isDefault": "false"}]'
       );


-- 创建 friend 表
CREATE TABLE friend (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT,
                        name VARCHAR(255) NOT NULL,
                        sex VARCHAR(10),
                        email VARCHAR(255) NOT NULL,
                        addresses JSON,
    -- 外键关联 user 表
                        owning_id BIGINT NOT NULL,
                        FOREIGN KEY ( owning_id) REFERENCES user(id)
);

-- 在 friend 表中新增测试数据
-- 插入一些测试数据到 `friend` 表，并关联到 `user` 表中的用户（假设用户ID为1）
INSERT INTO friend (user_id, name, sex, email, owning_id, addresses)
VALUES (NULL, 'Friend Name', '男', 'friend1@example.com', 1, '[{"id": 1, "formattedAddress": "789 Elm St", "longitude": 37.7749, "latitude": -122.4194, "isDefault": "true"}]'),
       (NULL, 'Another Friend', '女', 'friend2@example.com', 1, '[{"id": 2, "formattedAddress": "101 Oak St", "longitude": 37.7749, "latitude": -122.4194, "isDefault": "false"}]'),
       (NULL, 'Third Friend', 'Other', 'friend3@example.com', 1, '[{"id": 3, "formattedAddress": "123 Pine St", "longitude": 37.7749, "latitude": -122.4194, "isDefault": "false"}]');

-- 添加 'createUser' 字段，并添加注释
ALTER TABLE friend ADD COLUMN create_user BIGINT COMMENT '创建者ID';

-- 添加 'createTime' 字段，并添加注释
ALTER TABLE friend ADD COLUMN create_time DATETIME COMMENT '添加时间';

-- 添加 'updateUser' 字段，并添加注释
ALTER TABLE friend ADD COLUMN update_user BIGINT COMMENT '更新者ID';

-- 添加 'updateTime' 字段，并添加注释
ALTER TABLE friend ADD COLUMN update_time DATETIME COMMENT '更新时间';

-- 添加 'remark' 字段，并添加注释
ALTER TABLE friend ADD COLUMN remark TEXT COMMENT '备注信息';

-- 修改 paper 表
ALTER TABLE paper
    ADD COLUMN font_size VARCHAR(50) NOT NULL DEFAULT '' COMMENT '字体大小',
    ADD COLUMN translate_x VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'x偏移量',
    ADD COLUMN translate_y VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'y偏移量';

-- 创建颜色表（如果尚未创建）
CREATE TABLE IF NOT EXISTS font_color (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     hex_code VARCHAR(10) NOT NULL UNIQUE,
                                     rgb_value VARCHAR(50) NOT NULL,
                                     description VARCHAR(255)
);

-- 插入颜色数据
INSERT INTO font_color (hex_code, rgb_value, description)
VALUES
    ('#666666', 'RGB (102, 102, 102)', '墨色'),
    ('#A52328', 'RGB (165, 35, 39)', '朱砂色'),
    ('#F9E009', 'RGB (249, 224, 9)', '金色');

# signet
CREATE TABLE signet (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(255) NOT NULL
);
-- 修改 font 表
ALTER TABLE signet
    ADD COLUMN preview_image VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN file_path VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE user ADD COLUMN signets JSON;
ALTER TABLE user ADD COLUMN font_colors JSON;





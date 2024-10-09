
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



建表语句


新的建表语句
```
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(255) NOT NULL UNIQUE,
    nick_name VARCHAR(255),
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
INSERT INTO user (user_name, email, password, create_user, update_user) VALUES ('admin', 'Trashwbin@gmail.com', 'e10adc3949ba59abbe56e057f20f883e', 1, 1);






```

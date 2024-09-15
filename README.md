
建表语句



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
                      create_by VARCHAR(255),
                      create_time DATETIME,
                      update_by VARCHAR(255),
                      update_time DATETIME,
                      remark TEXT,
                      PRIMARY KEY (id)
);

insert into user (user_name, email, password) values ('admin', 'Trashwbin@gmail.com', '123456');






```

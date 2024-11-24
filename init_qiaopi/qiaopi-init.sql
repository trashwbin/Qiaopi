create table if not exists qiaopi.avatar
(
    id   bigint auto_increment comment 'id'
        primary key,
    name varchar(255) not null comment '头像名称',
    url  varchar(255) not null comment '头像预览图片'
)
    comment '头像' charset = utf8mb4;

create table qiaopi.bottle
(
    id             bigint auto_increment comment 'ID'
        primary key,
    user_id        bigint       null comment '用户ID',
    nick_name      varchar(50)  null comment '昵称',
    email          varchar(100) null comment '邮箱',
    sender_address json         null comment '寄件人地址',
    content        text         null comment '内容',
    is_picked      tinyint(1)   null comment '是否被捡起',
    create_time    timestamp    null comment '创建时间',
    update_time    timestamp    null comment '更新时间',
    create_user    varchar(50)  null comment '创建者',
    update_user    varchar(50)  null comment '更新者',
    bottle_url     varchar(255) null comment '瓶子链接',
    remark         varchar(255) null comment '备注'
)
    comment '漂流瓶表';

create table qiaopi.commodity
(
    id          bigint auto_increment comment 'ID'
        primary key,
    name        varchar(255) not null comment '商品名称',
    description text         null comment '商品描述',
    price       varchar(255) null comment '价格',
    image       varchar(255) null comment '图片链接',
    marketing   varchar(255) null comment '营销信息',
    link        varchar(255) null comment '商品链接'
)
    comment '商品表';

create table qiaopi.country
(
    id                   bigint unsigned auto_increment comment 'ID'
        primary key,
    country_name         varchar(50)     null comment '国家名称',
    country_name_english varchar(100)    null comment '国家英文名称',
    capital_name         varchar(50)     null comment '首都名称',
    capital_longitude    decimal(9, 6)   null comment '首都经度',
    capital_latitude     decimal(9, 6)   null comment '首都纬度'
)
    comment '国家表';

create table qiaopi.font
(
    id            int auto_increment comment 'ID'
        primary key,
    name          varchar(255)            not null comment '字体名称',
    preview_image varchar(255) default '' not null comment '预览图片',
    file_path     varchar(255) default '' not null comment '文件路径',
    price         int                     null comment '商品价格'
)
    comment '字体表';

create table qiaopi.font_color
(
    id            int auto_increment comment 'ID'
        primary key,
    hex_code      varchar(10)  not null comment '十六进制代码',
    rgb_value     varchar(50)  not null comment 'RGB值',
    description   varchar(255) null comment '描述',
    preview_image varchar(255) null comment '预览图片',
    price         int          null comment '商品价格',
    constraint hex_code
        unique (hex_code)
)
    comment '字体颜色表';

create table qiaopi.font_paper
(
    id         int not null comment 'ID'
        primary key,
    paper_id   int null comment '纸张ID',
    font_id    int null comment '字体ID',
    fit_number int null comment '适配数量'
)
    comment '字体纸张表';

create table qiaopi.friend
(
    id          int auto_increment comment 'ID'
        primary key,
    user_id     bigint       null comment '用户ID',
    name        varchar(255) null comment '姓名',
    sex         varchar(10)  null comment '性别',
    email       varchar(255) null comment '邮箱',
    addresses   json         null comment '地址',
    owning_id   bigint       null comment '拥有者ID',
    remark      text         null comment '备注',
    create_user bigint       null comment '创建者',
    create_time datetime     null comment '创建时间',
    update_user bigint       null comment '更新者',
    update_time datetime     null comment '更新时间',
    avatar      varchar(255) null comment '头像'
)
    comment '好友表';

create table qiaopi.friend_request
(
    id           bigint auto_increment comment 'ID'
        primary key,
    sender_id    bigint       null comment '发送者ID',
    receiver_id  bigint       null comment '接收者ID',
    status       int          null comment '状态',
    create_time  timestamp    null comment '创建时间',
    create_user  bigint       null comment '创建者',
    update_user  bigint       null comment '更新者',
    update_time  timestamp    null comment '更新时间',
    remark       varchar(255) null comment '备注',
    give_address json         null comment '提供的地址',
    content      varchar(255) null comment '内容',
    bottle_id    bigint       null comment '漂流瓶ID',
    letter_id    bigint       null comment '信件ID'
)
    comment '好友请求表';

create table qiaopi.function_card
(
    id                bigint auto_increment comment '主键'
        primary key,
    card_type         int          not null comment '卡片类型',
    card_name         varchar(255) not null comment '卡片名称',
    card_desc         varchar(255) null comment '卡片描述',
    card_preview_link varchar(255) null comment '卡片预览链接',
    card_status       int          not null comment '卡片状态',
    reduce_time       varchar(255) null comment '可减少时间',
    speed_rate        varchar(255) null comment '可加速速率',
    remark            varchar(255) null comment '备注',
    price             int          null comment '商品价格'
)
    comment '卡片信息表' charset = utf8mb4;

create table qiaopi.letter
(
    id                     bigint auto_increment comment 'ID'
        primary key,
    sender_user_id         bigint                   null comment '寄件人的用户ID',
    sender_name            varchar(255)             null comment '寄件人的姓名',
    recipient_email        varchar(255)             null comment '收件人的邮箱',
    recipient_user_id      bigint                   null comment '收件人的用户ID(非必需项)',
    recipient_name         varchar(255)             null comment '收件人的姓名',
    letter_content         text                     null comment '信的内容',
    letter_link            varchar(255)             null comment '信的链接',
    cover_link             varchar(255)             null comment '封面链接',
    sender_address         json                     null comment '寄件人地址',
    recipient_address      json                     null comment '收件人地址',
    expected_delivery_time datetime                 null comment '预计送达时间',
    status                 int                      null comment '信件状态(0:未发送 1:已发送,2:传递中,3:已送达)',
    delivery_progress      bigint       default 0   null comment '送信进度(0-10000)',
    read_status            int                      null comment '阅读状态(0:未读,1:已读)',
    create_user            bigint                   null comment '创建者',
    create_time            datetime                 null comment '创建时间',
    update_user            bigint                   null comment '更新者',
    update_time            datetime                 null comment '更新时间',
    remark                 varchar(255)             null comment '备注',
    sender_email           varchar(255)             null comment '寄件人邮箱',
    piggy_money            bigint       default 0   null comment '携带猪仔钱',
    letter_type            int          default 1   null comment '信件类型(1:竖版字体信件,2:横版信件)',
    speed_rate             varchar(255) default '1' null comment '加速倍率',
    reduce_time            varchar(255) default '0' null comment '减少的时间(单位:分钟)',
    delivery_time          datetime                 null comment '送达时间'
)
    comment '信件表';

create table qiaopi.paper
(
    id                    int          null comment 'ID',
    name                  varchar(255) null comment '名称',
    preview_image         varchar(255) null comment '预览图片',
    file_path             varchar(255) null comment '文件路径',
    font_size             varchar(50)  null comment '字体大小',
    translate_x           varchar(50)  null comment 'X轴平移',
    translate_y           varchar(50)  null comment 'Y轴平移',
    recipient_translate_x varchar(255) null comment '收件人X轴平移',
    recipient_translate_y varchar(255) null comment '收件人Y轴平移',
    sender_translate_x    varchar(255) null comment '寄件人X轴平移',
    sender_translate_y    varchar(255) null comment '寄件人Y轴平移',
    price                 int          null comment '价格',
    type                  int          null comment '类型'
)
    comment '纸张表';

create table qiaopi.question_user_status
(
    id                 bigint auto_increment comment 'ID'
        primary key,
    user_id            bigint                              not null comment '用户ID',
    question_set_1_id  int       default 0                 null comment '问题集1 ID',
    question_set_2_id  int       default 0                 null comment '问题集2 ID',
    question_set_3_id  int       default 0                 null comment '问题集3 ID',
    question_set_4_id  int       default 0                 null comment '问题集4 ID',
    question_set_5_id  int       default 0                 null comment '问题集5 ID',
    question_set_6_id  int       default 0                 null comment '问题集6 ID',
    question_set_7_id  int       default 0                 null comment '问题集7 ID',
    question_set_8_id  int       default 0                 null comment '问题集8 ID',
    question_set_9_id  int       default 0                 null comment '问题集9 ID',
    question_set_10_id int       default 0                 null comment '问题集10 ID',
    create_user        varchar(50)                         not null comment '创建者',
    update_user        varchar(50)                         not null comment '更新者',
    create_time        timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_time        timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    remark             varchar(255)                        null comment '备注',
    constraint uq_user_question_set
        unique (user_id)
)
    comment '用户问题集状态表';

create table qiaopi.questions
(
    id              bigint auto_increment comment 'ID'
        primary key,
    set_id          bigint                              not null comment '问题集ID',
    set_sequence_id int                                 not null comment '问题集序列ID',
    content         text                                not null comment '问题内容',
    option_a        varchar(255)                        not null comment '选项A',
    option_b        varchar(255)                        not null comment '选项B',
    option_c        varchar(255)                        not null comment '选项C',
    option_d        varchar(255)                        not null comment '选项D',
    correct_answer  char                                not null comment '正确答案',
    explanation     text                                null comment '解释',
    create_user     varchar(50)                         not null comment '创建者',
    create_time     timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_user     varchar(50)                         null comment '更新者',
    update_time     timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    remark          varchar(255)                        null comment '备注'
)
    comment '问题表';

create table qiaopi.signet
(
    id            int auto_increment comment 'ID'
        primary key,
    name          varchar(255)            not null comment '名称',
    preview_image varchar(255) default '' not null comment '预览图片',
    file_path     varchar(255) default '' not null comment '文件路径',
    price         int                     null comment '商品价格'
)
    comment '印章表';

create table qiaopi.user
(
    id             bigint auto_increment comment 'ID'
        primary key,
    username       varchar(255)            not null comment '用户名',
    nickname       varchar(255)            null comment '昵称',
    email          varchar(255)            not null comment '邮箱',
    sex            varchar(10)             null comment '性别',
    avatar         varchar(255)            null comment '头像',
    password       varchar(255)            null comment '密码',
    status         varchar(10) default '0' not null comment '状态',
    del_flag       varchar(10) default '0' not null comment '删除标志',
    login_ip       varchar(50)             null comment '登录IP',
    login_date     datetime                null comment '登录日期',
    create_user    bigint                  null comment '创建者',
    create_time    datetime                null comment '创建时间',
    update_user    bigint                  null comment '更新者',
    update_time    datetime                null comment '更新时间',
    remark         text                    null comment '备注',
    money          bigint      default 0   null comment '金钱',
    fonts          json                    null comment '字体',
    papers         json                    null comment '纸张',
    addresses      json                    null comment '地址',
    signets        json                    null comment '印章',
    font_colors    json                    null comment '字体颜色',
    function_cards json                    null comment '功能卡片',
    constraint uq_email
        unique (email),
    constraint uq_username
        unique (username)
)
    comment '用户表';


    
-- 创建数据库（如果不存在）
CREATE
    DATABASE IF NOT EXISTS smart_note DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 选择数据库
USE
    smart_note;

-- 建表语句
-- 用户表
CREATE TABLE `user`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，主键',
    `username`   VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名，唯一，可修改',
    `phone`      VARCHAR(20) UNIQUE COMMENT '手机号，必须',
    `email`      VARCHAR(100) UNIQUE COMMENT '邮箱，可选',
    `password`   VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `avatar`     VARCHAR(255) COMMENT '头像图片URL，有默认头像',
    `motto`      VARCHAR(200) COMMENT '座右铭',
    `is_banned`  TINYINT  DEFAULT 0 COMMENT '是否被封禁 0-正常 1-封禁',
    `is_deleted` TINYINT  DEFAULT 0 COMMENT '逻辑删除 0-正常 1-已删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '用户信息最后修改时间',

    -- 手机号和邮箱至少一个不为空
    CONSTRAINT chk_phone_or_email CHECK (phone IS NOT NULL OR email IS NOT NULL)
) COMMENT '用户表';


-- 笔记表
CREATE TABLE `note`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '笔记ID',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户ID，不可更改',
    `folder_id`  BIGINT       NOT NULL DEFAULT 0 COMMENT '文件夹id，默认0',
    `path`       VARCHAR(500) NOT NULL COMMENT '文件路径，如 /root/工作/笔记1',
    `title`      VARCHAR(200) NOT NULL COMMENT '笔记标题',
    `content`    LONGTEXT COMMENT '笔记正文，存Markdown',
    `visibility` VARCHAR(20)           DEFAULT 'PRIVATE' COMMENT '可见性: PRIVATE/FRIENDS/PUBLIC',
    `tags`       VARCHAR(500) COMMENT '标签',
    `is_deleted` TINYINT               DEFAULT 0 COMMENT '逻辑删除 0-正常 1-在回收站',
    `deleted_at` DATETIME COMMENT '进入回收站的时间',
    `created_at` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_path` (`path`(191)),
    INDEX `idx_created_at` (`created_at`)
) COMMENT '笔记表';

-- 文件夹表
CREATE TABLE `folder`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件夹ID',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户ID',
    `parent_id`  BIGINT       NOT NULL DEFAULT 0 COMMENT '所属文件夹',
    `path`       VARCHAR(500) NOT NULL COMMENT '文件路径，如 /root/工作',
    `name`       VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    `is_deleted` TINYINT               DEFAULT 0 COMMENT '逻辑删除 0-正常 1-在回收站',
    `deleted_at` DATETIME COMMENT '进入回收站的时间',
    `created_at` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_path` (`path`(191))
) COMMENT '文件夹表';

-- 好友关系表
CREATE TABLE `friendship`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT NOT NULL COMMENT '用户本人ID',
    `friend_id`  BIGINT NOT NULL COMMENT '好友ID',
    `group_name` VARCHAR(50) DEFAULT '默认' COMMENT '好友分组',
    `created_at` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友的时间',
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    INDEX `idx_user_id` (`user_id`)
) COMMENT '好友关系表';


-- 好友请求表
CREATE TABLE `friend_request`
(
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `requester_id` BIGINT NOT NULL COMMENT '发送申请的用户ID',
    `receiver_id`  BIGINT NOT NULL COMMENT '接收申请的用户ID',
    `status`       VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING待处理/ACCEPTED已接受/REJECTED已拒绝',
    `created_at`   DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `updated_at`   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态变更时间',
    UNIQUE KEY `uk_requester_receiver` (`requester_id`, `receiver_id`),
    INDEX `idx_receiver_id` (`receiver_id`)
) COMMENT '好友请求表';


-- 笔记权限表
CREATE TABLE `note_permission`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `note_id`    BIGINT NOT NULL COMMENT '笔记ID',
    `user_id`    BIGINT NOT NULL COMMENT '被授权的用户ID',
    `can_edit`   TINYINT  DEFAULT 0 COMMENT '是否可编辑 0只读 1可编辑',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    UNIQUE KEY `uk_note_user` (`note_id`, `user_id`), -- 笔记向同一个用户只能授权一次
    INDEX `idx_note_id` (`note_id`)
) COMMENT '笔记权限表';


-- 浏览历史表
CREATE TABLE `browse_history`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT NOT NULL COMMENT '用户ID',
    `note_id`    BIGINT NOT NULL COMMENT '被查看的笔记ID',
    `view_count` INT      DEFAULT 1 COMMENT '浏览次数',
    `browsed_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近浏览时间',
    UNIQUE KEY `uk_user_note` (`user_id`, `note_id`), -- 用户查看一篇笔记只能存在一次
    INDEX `idx_user_browsed` (`user_id`, `browsed_at`)
) COMMENT '浏览历史表';

-- ai总结表
CREATE TABLE `ai_summary`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `note_id`    BIGINT NOT NULL COMMENT '绑定的笔记ID',
    `summary`    LONGTEXT COMMENT 'ai总结内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '第一次总结的时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近一次总结的时间',
    UNIQUE KEY `uk_note_id` (`note_id`) -- 一篇笔记只能有一个索引
) COMMENT 'ai总结表';

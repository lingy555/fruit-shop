-- 聊天功能相关数据库表

-- 聊天会话表
CREATE TABLE IF NOT EXISTS chat_session (
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    shop_id BIGINT NOT NULL COMMENT '店铺ID',
    last_message_id BIGINT NULL COMMENT '最后一条消息ID',
    last_message_time DATETIME NULL COMMENT '最后一条消息时间',
    last_message_content TEXT NULL COMMENT '最后一条消息内容',
    user_unread_count INT DEFAULT 0 COMMENT '用户未读消息数',
    merchant_unread_count INT DEFAULT 0 COMMENT '商家未读消息数',
    status TINYINT DEFAULT 1 COMMENT '会话状态：1-活跃，0-关闭',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_last_message_time (last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT '会话ID',
    sender_type TINYINT NOT NULL COMMENT '发送者类型：1-用户，2-商家',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    message_type TINYINT DEFAULT 1 COMMENT '消息类型：1-文本，2-图片，3-商品',
    content TEXT NOT NULL COMMENT '消息内容',
    extra_data JSON NULL COMMENT '额外数据（如商品信息）',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    read_time DATETIME NULL COMMENT '阅读时间',
    
    INDEX idx_session_id (session_id),
    INDEX idx_sender (sender_type, sender_id),
    INDEX idx_send_time (send_time),
    INDEX idx_is_read (is_read),
    
    FOREIGN KEY (session_id) REFERENCES chat_session(session_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 用户聊天关系表（用于快速查找用户的聊天对象）
CREATE TABLE IF NOT EXISTS user_chat_relation (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    shop_id BIGINT NOT NULL COMMENT '店铺ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    last_contact_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后联系时间',
    
    UNIQUE KEY uk_user_merchant (user_id, merchant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_merchant_id (merchant_id),
    
    FOREIGN KEY (session_id) REFERENCES chat_session(session_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户聊天关系表';

-- 商家客服设置表
CREATE TABLE IF NOT EXISTS merchant_service_settings (
    settings_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    shop_id BIGINT NOT NULL COMMENT '店铺ID',
    auto_reply_enabled TINYINT DEFAULT 0 COMMENT '是否启用自动回复',
    auto_reply_message TEXT NULL COMMENT '自动回复消息',
    service_hours JSON NULL COMMENT '服务时间设置',
    max_response_time INT DEFAULT 300 COMMENT '最大响应时间（秒）',
    is_online TINYINT DEFAULT 1 COMMENT '是否在线',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_merchant (merchant_id),
    INDEX idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家客服设置表';

-- 插入一些示例数据
INSERT INTO merchant_service_settings (merchant_id, shop_id, auto_reply_enabled, auto_reply_message) 
VALUES (1001, 10, 1, '您好，欢迎咨询！我们会尽快回复您的消息。')
ON DUPLICATE KEY UPDATE auto_reply_enabled = VALUES(auto_reply_enabled);

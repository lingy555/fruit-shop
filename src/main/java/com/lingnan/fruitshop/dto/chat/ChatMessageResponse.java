package com.lingnan.fruitshop.dto.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {

    private Long messageId;
    private Long sessionId;
    private Integer senderType; // 1-用户，2-商家
    private Long senderId;
    private String senderName; // 发送者姓名
    private String senderAvatar; // 发送者头像
    private Integer messageType; // 1-文本，2-图片，3-商品
    private String content;
    private String extraData; // JSON格式的额外数据
    private Integer isRead; // 0-未读，1-已读
    private LocalDateTime sendTime;
    private LocalDateTime readTime;
}

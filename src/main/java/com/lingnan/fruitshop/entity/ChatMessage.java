package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_message")
public class ChatMessage {

    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    @TableField("session_id")
    private Long sessionId;

    @TableField("sender_type")
    private Integer senderType; // 1-用户，2-商家

    @TableField("sender_id")
    private Long senderId;

    @TableField("message_type")
    private Integer messageType; // 1-文本，2-图片，3-商品

    @TableField("content")
    private String content;

    @TableField("extra_data")
    private String extraData; // JSON格式的额外数据

    @TableField("is_read")
    private Integer isRead; // 0-未读，1-已读

    @TableField("send_time")
    private LocalDateTime sendTime;

    @TableField("read_time")
    private LocalDateTime readTime;
}

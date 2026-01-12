package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_session")
public class ChatSession {

    @TableId(value = "session_id", type = IdType.AUTO)
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("shop_id")
    private Long shopId;

    @TableField("last_message_id")
    private Long lastMessageId;

    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    @TableField("last_message_content")
    private String lastMessageContent;

    @TableField("user_unread_count")
    private Integer userUnreadCount;

    @TableField("merchant_unread_count")
    private Integer merchantUnreadCount;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

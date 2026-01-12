package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_chat_relation")
public class UserChatRelation {

    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long relationId;

    @TableField("user_id")
    private Long userId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("shop_id")
    private Long shopId;

    @TableField("session_id")
    private Long sessionId;

    @TableField("last_contact_time")
    private LocalDateTime lastContactTime;
}

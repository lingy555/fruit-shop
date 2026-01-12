package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("merchant_service_settings")
public class MerchantServiceSettings {

    @TableId(value = "settings_id", type = IdType.AUTO)
    private Long settingsId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("shop_id")
    private Long shopId;

    @TableField("auto_reply_enabled")
    private Integer autoReplyEnabled;

    @TableField("auto_reply_message")
    private String autoReplyMessage;

    @TableField("service_hours")
    private String serviceHours; // JSON格式

    @TableField("max_response_time")
    private Integer maxResponseTime;

    @TableField("is_online")
    private Integer isOnline;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund")
public class Refund {

    @TableId(value = "refund_id", type = IdType.AUTO)
    private Long refundId;

    private String refundNo;

    private Long orderId;

    private Long orderItemId;

    private Long userId;

    private Long shopId;

    private Integer type;

    private String reason;

    private String description;

    private String imagesJson;

    private BigDecimal refundAmount;

    private Integer status;

    private String merchantReply;

    private String rejectReason;

    private Integer arbitrateResult;

    private String arbitrateReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

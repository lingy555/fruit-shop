package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {

    @TableId(value = "coupon_id", type = IdType.AUTO)
    private Long couponId;

    private String couponName;

    private Integer couponType;

    private Integer discountType;

    private BigDecimal discountValue;

    private BigDecimal minAmount;

    private Integer totalCount;

    private Integer receivedCount;

    private Integer usedCount;

    private Integer limitPerUser;

    private Integer validDays;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

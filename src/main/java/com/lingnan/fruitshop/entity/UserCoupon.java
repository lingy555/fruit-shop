package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coupon")
public class UserCoupon {

    @TableId(value = "user_coupon_id", type = IdType.AUTO)
    private Long userCouponId;

    private Long userId;

    private Long couponId;

    private Integer status;

    private LocalDateTime receiveTime;

    private LocalDateTime useTime;

    private Long orderId;
}

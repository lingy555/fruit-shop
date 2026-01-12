package com.lingnan.fruitshop.dto.customer.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CustomerMyCouponItemResponse {

    private Long userCouponId;
    private Long couponId;
    private String couponName;
    private Integer discountType;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer status;
    private String statusText;
    private String receiveTime;
    private String useTime;
    private String expireTime;
    private String description;
}

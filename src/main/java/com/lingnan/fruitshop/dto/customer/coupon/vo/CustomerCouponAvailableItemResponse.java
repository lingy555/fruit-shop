package com.lingnan.fruitshop.dto.customer.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CustomerCouponAvailableItemResponse {

    private Long couponId;
    private String couponName;
    private Integer couponType;
    private Integer discountType;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer totalCount;
    private Integer receivedCount;
    private Integer remainingCount;
    private Integer limitPerUser;
    private Integer receivedByMe;
    private Boolean canReceive;
    private String startTime;
    private String endTime;
    private String description;
}

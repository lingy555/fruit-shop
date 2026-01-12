package com.lingnan.fruitshop.dto.admin.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminCouponListItemResponse {

    private Long couponId;
    private String couponName;
    private Integer couponType;
    private Integer discountType;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer totalCount;
    private Integer receivedCount;
    private Integer usedCount;
    private String startTime;
    private String endTime;
    private Integer status;
    private String createTime;
}

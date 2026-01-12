package com.lingnan.fruitshop.dto.admin.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminCouponAddRequest {

    @NotBlank
    private String couponName;

    @NotNull
    private Integer couponType;

    @NotNull
    private Integer discountType;

    @NotNull
    private BigDecimal discountValue;

    @NotNull
    private BigDecimal minAmount;

    @NotNull
    private Integer totalCount;

    @NotNull
    private Integer limitPerUser;

    @NotNull
    private Integer validDays;

    private String startTime;

    private String endTime;

    private String description;
}

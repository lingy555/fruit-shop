package com.lingnan.fruitshop.dto.admin.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminPlatformDataResponse {

    private long totalUserCount;
    private long totalMerchantCount;
    private long totalProductCount;
    private long totalOrderCount;
    private BigDecimal totalOrderAmount;
}

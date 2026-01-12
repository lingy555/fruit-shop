package com.lingnan.fruitshop.dto.admin.merchant.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminMerchantStatisticsResponse {

    private long orderCount;
    private BigDecimal salesAmount;
    private long refundCount;
    private BigDecimal refundAmount;
}

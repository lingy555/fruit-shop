package com.lingnan.fruitshop.dto.admin.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminChartDataItemResponse {

    private String date;

    private Long orderCount;
    private BigDecimal orderAmount;

    private Long newUserCount;
    private Long totalUserCount;

    private Long newMerchantCount;
    private Long totalMerchantCount;

    private String categoryName;
    private Long productCount;
    private Long productSales;
}

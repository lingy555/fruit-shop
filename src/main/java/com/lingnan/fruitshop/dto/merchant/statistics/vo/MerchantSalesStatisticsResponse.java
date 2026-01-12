package com.lingnan.fruitshop.dto.merchant.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantSalesStatisticsResponse {

    private long totalOrderCount;
    private BigDecimal totalOrderAmount;
    private long totalPayOrderCount;
    private BigDecimal totalPayOrderAmount;
    private BigDecimal avgOrderAmount;
    private long refundCount;
    private BigDecimal refundAmount;
    private double refundRate;
    private List<TrendData> trendData;
    private List<CategoryData> categoryData;

    @Data
    @AllArgsConstructor
    public static class TrendData {
        private String date;
        private long orderCount;
        private BigDecimal orderAmount;
        private long payOrderCount;
        private BigDecimal payOrderAmount;
    }

    @Data
    @AllArgsConstructor
    public static class CategoryData {
        private String categoryName;
        private long orderCount;
        private BigDecimal orderAmount;
        private double percentage;
    }
}

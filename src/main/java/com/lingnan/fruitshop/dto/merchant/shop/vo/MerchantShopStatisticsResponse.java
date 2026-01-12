package com.lingnan.fruitshop.dto.merchant.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantShopStatisticsResponse {

    private TodayData todayData;
    private TotalData totalData;
    private List<TrendData> trendData;

    @Data
    @AllArgsConstructor
    public static class TodayData {
        private long orderCount;
        private BigDecimal orderAmount;
        private long payOrderCount;
        private BigDecimal payOrderAmount;
        private long visitCount;
        private long newFanCount;
    }

    @Data
    @AllArgsConstructor
    public static class TotalData {
        private long totalOrderCount;
        private BigDecimal totalOrderAmount;
        private long totalProductCount;
        private long totalFanCount;
    }

    @Data
    @AllArgsConstructor
    public static class TrendData {
        private String date;
        private long orderCount;
        private BigDecimal orderAmount;
        private long visitCount;
    }
}

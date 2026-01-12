package com.lingnan.fruitshop.dto.merchant.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MerchantOverviewResponse {

    private TodayData todayData;
    private CompareYesterday compareYesterday;
    private PendingCount pendingCount;

    @Data
    @AllArgsConstructor
    public static class TodayData {
        private long orderCount;
        private BigDecimal orderAmount;
        private long payOrderCount;
        private BigDecimal payOrderAmount;
        private long visitCount;
        private long newFanCount;
        private long refundCount;
    }

    @Data
    @AllArgsConstructor
    public static class CompareYesterday {
        private double orderCountRate;
        private double orderAmountRate;
        private double visitCountRate;
    }

    @Data
    @AllArgsConstructor
    public static class PendingCount {
        private long waitDeliverCount;
        private long refundingCount;
        private long lowStockCount;
        private long unreplyCommentCount;
    }
}

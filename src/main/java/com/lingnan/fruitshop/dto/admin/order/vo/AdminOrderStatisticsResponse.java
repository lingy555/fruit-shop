package com.lingnan.fruitshop.dto.admin.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminOrderStatisticsResponse {

    private Summary summary;
    private List<TrendItem> trend;

    @Data
    @AllArgsConstructor
    public static class Summary {
        private long orderCount;
        private BigDecimal orderAmount;
    }

    @Data
    @AllArgsConstructor
    public static class TrendItem {
        private String date;
        private long orderCount;
        private BigDecimal orderAmount;
    }
}

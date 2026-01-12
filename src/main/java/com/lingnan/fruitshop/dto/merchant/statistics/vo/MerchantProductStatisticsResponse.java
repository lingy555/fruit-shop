package com.lingnan.fruitshop.dto.merchant.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantProductStatisticsResponse {

    private List<TopSalesProduct> topSalesProducts;
    private List<LowStockProduct> lowStockProducts;

    @Data
    @AllArgsConstructor
    public static class TopSalesProduct {
        private Long productId;
        private String productName;
        private String image;
        private Long sales;
        private BigDecimal amount;
        private Integer stock;
    }

    @Data
    @AllArgsConstructor
    public static class LowStockProduct {
        private Long productId;
        private String productName;
        private String image;
        private Integer stock;
        private Long sales;
    }
}

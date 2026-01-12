package com.lingnan.fruitshop.dto.merchant.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantCustomerStatisticsResponse {

    private long totalCustomerCount;
    private long newCustomerCount;
    private long activeCustomerCount;
    private List<TopCustomer> topCustomers;

    @Data
    @AllArgsConstructor
    public static class TopCustomer {
        private Long userId;
        private String username;
        private long orderCount;
        private BigDecimal totalAmount;
        private String lastOrderTime;
    }
}

package com.lingnan.fruitshop.dto.merchant.refund.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantRefundListResponse {

    private long total;
    private StatusCount statusCount;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class StatusCount {
        private long pending;
        private long processing;
        private long finished;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long refundId;
        private String refundNo;
        private Long orderId;
        private String orderNo;
        private Long userId;
        private String username;
        private Integer type;
        private String typeText;
        private String reason;
        private String description;
        private List<String> images;
        private BigDecimal refundAmount;
        private Integer status;
        private String statusText;
        private Product product;
        private String createTime;
    }

    @Data
    @AllArgsConstructor
    public static class Product {
        private String productName;
        private String image;
        private BigDecimal price;
        private Integer quantity;
    }
}

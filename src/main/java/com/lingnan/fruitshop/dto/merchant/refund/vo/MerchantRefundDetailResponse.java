package com.lingnan.fruitshop.dto.merchant.refund.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantRefundDetailResponse {

    private Long refundId;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long orderItemId;
    private Long userId;
    private String username;
    private String userPhone;
    private Integer type;
    private String typeText;
    private String reason;
    private String description;
    private List<String> images;
    private BigDecimal refundAmount;
    private Integer status;
    private String statusText;
    private Product product;
    private List<Timeline> timeline;
    private String merchantReply;
    private String createTime;

    @Data
    @AllArgsConstructor
    public static class Product {
        private Long productId;
        private String productName;
        private String image;
        private String specs;
        private BigDecimal price;
        private Integer quantity;
    }

    @Data
    @AllArgsConstructor
    public static class Timeline {
        private String time;
        private String status;
        private String description;
    }
}

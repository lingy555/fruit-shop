package com.lingnan.fruitshop.dto.admin.refund.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminRefundDetailResponse {

    private Long refundId;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long orderItemId;
    private Long userId;
    private String username;
    private String userPhone;
    private Long shopId;
    private String shopName;
    private Integer type;
    private String typeText;
    private String reason;
    private String description;
    private List<String> images;
    private BigDecimal refundAmount;
    private Integer status;
    private String statusText;
    private String merchantReply;
    private String rejectReason;
    private Integer arbitrateResult;
    private String arbitrateReason;
    private Product product;
    private List<Timeline> timeline;
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

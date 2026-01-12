package com.lingnan.fruitshop.dto.admin.refund.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminRefundListResponse {

    private long total;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long refundId;
        private String refundNo;
        private Long orderId;
        private String orderNo;
        private Long userId;
        private String username;
        private Long shopId;
        private String shopName;
        private Integer type;
        private String typeText;
        private String reason;
        private BigDecimal refundAmount;
        private Integer status;
        private String statusText;
        private String createTime;
    }
}

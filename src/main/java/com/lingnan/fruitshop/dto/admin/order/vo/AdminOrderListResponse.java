package com.lingnan.fruitshop.dto.admin.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminOrderListResponse {

    private long total;
    private StatusCount statusCount;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class StatusCount {
        private long waitPay;
        private long waitDeliver;
        private long waitReceive;
        private long finished;
        private long cancelled;
        private long refunding;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long orderId;
        private String orderNo;
        private Long userId;
        private String username;
        private Long shopId;
        private String shopName;
        private Integer status;
        private String statusText;
        private BigDecimal totalAmount;
        private BigDecimal actualAmount;
        private String paymentMethod;
        private String createTime;
        private String payTime;
    }
}

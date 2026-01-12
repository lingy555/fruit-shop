package com.lingnan.fruitshop.dto.customer.order.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderListResponse {

    private long total;
    private StatusCount statusCount;
    private List<OrderItem> list;

    @Data
    @AllArgsConstructor
    public static class StatusCount {
        private long waitPay;
        private long waitDeliver;
        private long waitReceive;
        private long waitComment;
    }

    @Data
    @AllArgsConstructor
    public static class OrderItem {
        private Long orderId;
        private String orderNo;
        private Integer status;
        private String statusText;
        private Long shopId;
        private String shopName;
        private BigDecimal totalAmount;
        private BigDecimal shippingFee;
        private BigDecimal actualAmount;
        private String paymentMethod;
        private String createTime;
        private String payTime;
        private List<Item> items;
        private List<String> buttons;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long orderItemId;
        private Long productId;
        private Long skuId;
        private String productName;
        private String image;
        private BigDecimal price;
        private String specs;
        private Integer quantity;
        private BigDecimal subtotal;

        @JsonProperty("canComment")
        private boolean canComment;
    }
}

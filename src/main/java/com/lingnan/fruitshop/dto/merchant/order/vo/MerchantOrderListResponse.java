package com.lingnan.fruitshop.dto.merchant.order.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantOrderListResponse {

    private long total;
    private StatusCount statusCount;
    private List<OrderItem> list;

    @Data
    @AllArgsConstructor
    public static class StatusCount {
        private long waitDeliver;
        private long waitReceive;
        private long finished;
        private long refunding;
    }

    @Data
    @AllArgsConstructor
    public static class OrderItem {
        private Long orderId;
        private String orderNo;
        private Long userId;
        private String username;
        private Integer status;
        private String statusText;
        private BigDecimal totalAmount;
        private BigDecimal shippingFee;
        private BigDecimal actualAmount;
        private String paymentMethod;
        private String paymentMethodText;
        private String remark;
        private String createTime;
        private String payTime;
        private Address address;
        private List<Item> items;

        @JsonProperty("canDeliver")
        private boolean canDeliver;

        @JsonProperty("canCancel")
        private boolean canCancel;
    }

    @Data
    @AllArgsConstructor
    public static class Address {
        private String receiverName;
        private String receiverPhone;
        private String fullAddress;
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
    }
}

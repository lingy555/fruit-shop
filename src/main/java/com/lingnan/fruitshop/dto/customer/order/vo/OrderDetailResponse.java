package com.lingnan.fruitshop.dto.customer.order.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderDetailResponse {

    private Long orderId;
    private String orderNo;
    private Integer status;
    private String statusText;
    private Long shopId;
    private String shopName;
    private String shopPhone;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal couponDiscount;
    private BigDecimal pointsDiscount;
    private BigDecimal actualAmount;
    private String paymentMethod;
    private String paymentMethodText;
    private String remark;
    private String createTime;
    private String payTime;
    private String deliverTime;
    private String receiveTime;
    private String finishTime;
    private String cancelTime;
    private Address address;
    private Logistics logistics;
    private List<Item> items;
    private List<String> buttons;

    @Data
    @AllArgsConstructor
    public static class Address {
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String city;
        private String district;
        private String detail;
    }

    @Data
    @AllArgsConstructor
    public static class Logistics {
        private String expressCompany;
        private String expressNo;
        private String currentStatus;
        private List<Trace> traces;
    }

    @Data
    @AllArgsConstructor
    public static class Trace {
        private String time;
        private String status;
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

        @JsonProperty("canRefund")
        private boolean canRefund;
    }
}

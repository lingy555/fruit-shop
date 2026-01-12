package com.lingnan.fruitshop.dto.admin.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminOrderDetailResponse {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private String username;
    private String userPhone;
    private Long shopId;
    private String shopName;
    private Integer status;
    private String statusText;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal couponDiscount;
    private BigDecimal pointsDiscount;
    private BigDecimal actualAmount;
    private String paymentMethod;
    private String paymentMethodText;
    private String remark;
    private String merchantRemark;
    private String createTime;
    private String payTime;
    private String deliverTime;
    private String receiveTime;
    private String finishTime;
    private String cancelTime;
    private Address address;
    private Logistics logistics;
    private List<Item> items;
    private List<Timeline> timeline;

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
        private BigDecimal costPrice;
        private BigDecimal profit;
    }

    @Data
    @AllArgsConstructor
    public static class Timeline {
        private String time;
        private String status;
    }
}

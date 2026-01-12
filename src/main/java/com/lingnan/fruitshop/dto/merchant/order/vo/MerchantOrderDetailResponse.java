package com.lingnan.fruitshop.dto.merchant.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantOrderDetailResponse {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private String username;
    private String userPhone;
    private Integer status;
    private String statusText;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal couponDiscount;
    private BigDecimal actualAmount;
    private String paymentMethod;
    private String paymentMethodText;
    private String remark;
    private String createTime;
    private String payTime;
    private String deliverTime;
    private String receiveTime;
    private String finishTime;
    private Address address;
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

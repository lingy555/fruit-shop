package com.lingnan.fruitshop.dto.customer.order.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderConfirmResponse {

    private Address address;
    private List<ShopOrderItems> orderItems;
    private List<AvailableCoupon> availableCoupons;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal couponDiscount;
    private BigDecimal pointsDiscount;
    private BigDecimal actualAmount;
    private int availablePoints;
    private int pointsRate;

    @Data
    @AllArgsConstructor
    public static class Address {
        private Long addressId;
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String city;
        private String district;
        private String detail;

        @JsonProperty("isDefault")
        private boolean isDefault;
    }

    @Data
    @AllArgsConstructor
    public static class ShopOrderItems {
        private Long shopId;
        private String shopName;
        private List<ProductItem> products;
        private BigDecimal shippingFee;
        private BigDecimal shopTotal;
    }

    @Data
    @AllArgsConstructor
    public static class AvailableCoupon {
        private Long couponId;
        private String couponName;
        private BigDecimal discountAmount;
        private BigDecimal minAmount;
    }

    @Data
    @AllArgsConstructor
    public static class ProductItem {
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

package com.lingnan.fruitshop.dto.customer.cart.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class CartListResponse {

    private long totalCount;
    private long selectedCount;
    private BigDecimal totalAmount;
    private List<Item> items;
    private List<Item> invalidItems;

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long cartId;
        private Long productId;
        private Long skuId;
        private String productName;
        private String image;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String specs;
        private Integer quantity;
        private Integer stock;
        private boolean selected;

        @JsonProperty("isValid")
        private boolean isValid;
        private Long shopId;
        private String shopName;
    }
}

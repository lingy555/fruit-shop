package com.lingnan.fruitshop.dto.customer.product.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ProductDetailResponse {

    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long shopId;
    private String shopName;
    private String shopLogo;
    private BigDecimal shopScore;
    private long productCount;
    private long fanCount;
    private String image;
    private List<String> images;
    private String videoUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Long sales;
    private String unit;
    private String weight;
    private String description;
    private String detail;
    private BigDecimal score;
    private Long commentCount;
    private List<String> tags;
    private List<SpecItem> specifications;
    private List<SkuItem> skuList;
    private DeliveryInfo deliveryInfo;

    @JsonProperty("isCollect")
    private boolean isCollect;
    private long collectCount;
    private long viewCount;
    private String createTime;

    @Data
    @AllArgsConstructor
    public static class SpecItem {
        private Long specId;
        private String specName;
        private List<String> options;
    }

    @Data
    @AllArgsConstructor
    public static class SkuItem {
        private Long skuId;
        private BigDecimal price;
        private Integer stock;
        private Map<String, Object> specs;
    }

    @Data
    @AllArgsConstructor
    public static class DeliveryInfo {
        private boolean freeShipping;
        private BigDecimal shippingFee;
        private String deliveryTime;
    }
}

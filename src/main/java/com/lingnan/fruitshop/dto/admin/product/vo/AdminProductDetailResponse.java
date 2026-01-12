package com.lingnan.fruitshop.dto.admin.product.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AdminProductDetailResponse {

    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long shopId;
    private String shopName;
    private String image;
    private List<String> images;
    private String videoUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal costPrice;
    private Integer stock;
    private Long sales;
    private String unit;
    private String weight;
    private String description;
    private String detail;
    private List<String> tags;
    private Integer status;
    private String statusText;
    private String auditRemark;
    private BigDecimal score;
    private Long commentCount;
    private Long viewCount;
    private List<SpecItem> specifications;
    private List<SkuItem> skuList;
    private DeliveryInfo deliveryInfo;
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
        private String skuCode;
        private BigDecimal price;
        private Integer stock;
        private String image;
        private Integer status;
        private Map<String, Object> specs;
    }

    @Data
    @AllArgsConstructor
    public static class DeliveryInfo {
        @JsonProperty("freeShipping")
        private boolean freeShipping;
        private BigDecimal shippingFee;
        private String shippingTemplate;
    }
}

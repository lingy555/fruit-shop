package com.lingnan.fruitshop.dto.merchant.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class MerchantProductAddRequest {

    @NotBlank
    private String productName;

    @NotNull
    private Long categoryId;

    private String image;

    private List<String> images;

    private String videoUrl;

    @NotNull
    private BigDecimal price;

    private BigDecimal originalPrice;

    private BigDecimal costPrice;

    @NotNull
    @Min(0)
    private Integer stock;

    private String unit;

    private String weight;

    private String description;

    private String detail;

    private List<String> tags;

    private List<Specification> specifications;

    private List<Sku> skuList;

    @NotNull
    private Boolean freeShipping;

    @NotNull
    private BigDecimal shippingFee;

    @Data
    public static class Specification {
        @NotBlank
        private String specName;
        @NotNull
        private List<String> options;
    }

    @Data
    public static class Sku {
        private String skuCode;
        @NotNull
        private BigDecimal price;
        @NotNull
        @Min(0)
        private Integer stock;
        private String image;
        private Map<String, Object> specs;
    }
}

package com.lingnan.fruitshop.dto.customer.home.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class HomeIndexResponse {

    private List<BannerItem> banners;
    private List<CategoryItem> categories;
    private List<ProductItem> hotProducts;
    private List<ProductItem> newProducts;
    private List<ProductItem> recommendProducts;

    @Data
    @AllArgsConstructor
    public static class BannerItem {
        private Long bannerId;
        private String image;
        private String title;
        private String linkType;
        private String linkValue;
        private Integer sort;
    }

    @Data
    @AllArgsConstructor
    public static class CategoryItem {
        private Long categoryId;
        private String categoryName;
        private String icon;
        private Long productCount;
    }

    @Data
    @AllArgsConstructor
    public static class ProductItem {
        private Long productId;
        private String productName;
        private String image;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Long sales;
        private String shopName;
    }
}

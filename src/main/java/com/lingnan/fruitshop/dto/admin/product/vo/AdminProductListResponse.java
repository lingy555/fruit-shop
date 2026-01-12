package com.lingnan.fruitshop.dto.admin.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminProductListResponse {

    private long total;
    private StatusCount statusCount;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class StatusCount {
        private long pending;
        private long online;
        private long offline;
        private long rejected;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long productId;
        private String productName;
        private Long categoryId;
        private String categoryName;
        private Long shopId;
        private String shopName;
        private String image;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer stock;
        private Long sales;
        private Integer status;
        private String statusText;
        private BigDecimal score;
        private Long commentCount;
        private Long viewCount;
        private String createTime;
    }
}

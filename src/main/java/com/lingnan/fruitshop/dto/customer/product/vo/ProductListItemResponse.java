package com.lingnan.fruitshop.dto.customer.product.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ProductListItemResponse {

    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long shopId;
    private String shopName;
    private String image;
    private List<String> images;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Long sales;
    private String unit;
    private String weight;
    private BigDecimal score;
    private Long commentCount;
    private List<String> tags;

    @JsonProperty("isCollect")
    private boolean isCollect;
}

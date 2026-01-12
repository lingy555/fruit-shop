package com.lingnan.fruitshop.dto.merchant.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MerchantProductListItemResponse {

    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
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
    private String updateTime;
}

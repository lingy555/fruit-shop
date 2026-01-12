package com.lingnan.fruitshop.dto.customer.shop.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ShopDetailResponse {

    private Long shopId;
    private String shopName;
    private String logo;
    private String banner;
    private String description;
    private BigDecimal score;
    private long productCount;
    private long sales;
    private long fanCount;

    @JsonProperty("isFavorite")
    private boolean isFavorite;
    private String createTime;
}

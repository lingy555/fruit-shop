package com.lingnan.fruitshop.dto.customer.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MyFavoriteShopItemResponse {

    private Long shopId;
    private String shopName;
    private String logo;
    private BigDecimal score;
    private long fanCount;
}

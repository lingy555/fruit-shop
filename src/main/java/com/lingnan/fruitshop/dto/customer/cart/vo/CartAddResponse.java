package com.lingnan.fruitshop.dto.customer.cart.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartAddResponse {

    private Long cartId;
    private long totalCount;
}

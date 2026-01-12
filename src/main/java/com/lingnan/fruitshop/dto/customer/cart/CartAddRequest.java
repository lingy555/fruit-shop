package com.lingnan.fruitshop.dto.customer.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;
}

package com.lingnan.fruitshop.dto.customer.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartUpdateQuantityRequest {

    @NotNull
    private Long cartId;

    @NotNull
    @Min(1)
    private Integer quantity;
}

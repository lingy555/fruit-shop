package com.lingnan.fruitshop.dto.customer.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartSelectAllRequest {

    @NotNull
    private Boolean selected;
}

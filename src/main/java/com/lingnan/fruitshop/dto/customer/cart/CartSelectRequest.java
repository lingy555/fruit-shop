package com.lingnan.fruitshop.dto.customer.cart;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartSelectRequest {

    @NotEmpty
    private List<Long> cartIds;

    @NotNull
    private Boolean selected;
}

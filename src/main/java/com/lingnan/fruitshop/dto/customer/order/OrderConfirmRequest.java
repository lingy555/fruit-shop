package com.lingnan.fruitshop.dto.customer.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmRequest {

    @NotEmpty
    private List<Long> cartIds;

    private String buyType;
}

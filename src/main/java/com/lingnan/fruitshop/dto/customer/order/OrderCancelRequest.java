package com.lingnan.fruitshop.dto.customer.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCancelRequest {

    @NotBlank
    private String reason;
}

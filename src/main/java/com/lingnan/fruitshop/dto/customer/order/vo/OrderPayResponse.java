package com.lingnan.fruitshop.dto.customer.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderPayResponse {

    private String paymentUrl;
    private String qrCode;
}

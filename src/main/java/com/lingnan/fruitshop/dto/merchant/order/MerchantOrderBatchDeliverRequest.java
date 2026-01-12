package com.lingnan.fruitshop.dto.merchant.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MerchantOrderBatchDeliverRequest {

    @NotEmpty
    private List<MerchantOrderDeliverRequest> deliverList;
}

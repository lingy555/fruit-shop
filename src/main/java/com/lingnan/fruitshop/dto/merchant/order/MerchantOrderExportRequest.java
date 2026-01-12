package com.lingnan.fruitshop.dto.merchant.order;

import lombok.Data;

@Data
public class MerchantOrderExportRequest {

    private Integer status;

    private String startTime;

    private String endTime;
}

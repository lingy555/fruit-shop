package com.lingnan.fruitshop.dto.merchant.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MerchantRegisterResponse {

    private Long applicationId;
    private String status;
}

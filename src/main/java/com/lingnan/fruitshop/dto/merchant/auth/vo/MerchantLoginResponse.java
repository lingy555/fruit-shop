package com.lingnan.fruitshop.dto.merchant.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MerchantLoginResponse {

    private String token;
    private long tokenExpire;
    private MerchantInfo merchantInfo;

    @Data
    @AllArgsConstructor
    public static class MerchantInfo {
        private Long merchantId;
        private Long shopId;
        private String shopName;
        private String contactName;
        private String contactPhone;
        private Integer status;
        private Integer shopStatus;
    }
}

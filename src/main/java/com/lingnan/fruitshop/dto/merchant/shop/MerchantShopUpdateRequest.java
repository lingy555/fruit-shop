package com.lingnan.fruitshop.dto.merchant.shop;

import lombok.Data;

@Data
public class MerchantShopUpdateRequest {

    private String shopName;
    private String logo;
    private String banner;
    private String description;
    private String province;
    private String city;
    private String district;
    private String address;
    private String contactPhone;
    private String businessHours;
}

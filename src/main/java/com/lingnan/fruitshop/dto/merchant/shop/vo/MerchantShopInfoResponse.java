package com.lingnan.fruitshop.dto.merchant.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MerchantShopInfoResponse {

    private Long shopId;
    private String shopName;
    private String logo;
    private String banner;
    private String description;
    private String province;
    private String city;
    private String district;
    private String address;
    private String contactName;
    private String contactPhone;
    private String businessHours;
    private Integer status;
    private BigDecimal score;
    private long productCount;
    private BigDecimal totalSales;
    private long fanCount;
    private String createTime;
    private String businessLicense;
    private List<String> qualifications;
}

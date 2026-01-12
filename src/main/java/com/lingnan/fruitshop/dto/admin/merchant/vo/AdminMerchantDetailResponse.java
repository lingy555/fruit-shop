package com.lingnan.fruitshop.dto.admin.merchant.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminMerchantDetailResponse {

    private Long merchantId;
    private Long shopId;
    private String shopName;
    private String logo;
    private String banner;
    private String description;
    private String contactName;
    private String contactPhone;
    private String email;
    private String province;
    private String city;
    private String district;
    private String address;
    private String businessLicense;
    private String idCardFront;
    private String idCardBack;
    private List<String> qualifications;
    private Integer status;
    private String statusText;
    private BigDecimal score;
    private long productCount;
    private BigDecimal totalSales;
    private long totalOrderCount;
    private long fanCount;
    private String createTime;
    private String auditTime;
    private String auditRemark;
}

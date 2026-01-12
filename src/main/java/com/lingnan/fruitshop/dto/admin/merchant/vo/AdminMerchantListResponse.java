package com.lingnan.fruitshop.dto.admin.merchant.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminMerchantListResponse {

    private long total;
    private StatusCount statusCount;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class StatusCount {
        private long pending;
        private long normal;
        private long disabled;
        private long rejected;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long merchantId;
        private Long shopId;
        private String shopName;
        private String logo;
        private String contactName;
        private String contactPhone;
        private String email;
        private String province;
        private String city;
        private Integer status;
        private String statusText;
        private BigDecimal score;
        private long productCount;
        private BigDecimal totalSales;
        private long fanCount;
        private String createTime;
        private String auditTime;
    }
}

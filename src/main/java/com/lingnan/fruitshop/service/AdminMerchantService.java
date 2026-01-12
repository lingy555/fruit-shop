package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.merchant.AdminMerchantAuditRequest;
import com.lingnan.fruitshop.dto.admin.merchant.AdminMerchantUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantDetailResponse;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantListResponse;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantStatisticsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AdminMerchantService {
    AdminMerchantListResponse list(int page, int pageSize, String keyword, Integer status, String province, String city, String startTime, String endTime);
    AdminMerchantDetailResponse detail(long merchantId);
    void audit(AdminMerchantAuditRequest req);
    void updateStatus(AdminMerchantUpdateStatusRequest req);
    AdminMerchantStatisticsResponse statistics(long merchantId, String dateType);
}

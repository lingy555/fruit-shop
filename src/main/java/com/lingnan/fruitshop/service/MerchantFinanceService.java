package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.merchant.finance.vo.MerchantFinanceBillItemResponse;
import com.lingnan.fruitshop.dto.merchant.finance.vo.MerchantFinanceOverviewResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MerchantFinanceService {
    MerchantFinanceOverviewResponse overview(long shopId);
    PageResponse<MerchantFinanceBillItemResponse> bills(long shopId, int page, int pageSize, Integer type, String startDate, String endDate);
}

package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.merchant.statistics.vo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface MerchantStatisticsService {
    MerchantOverviewResponse overview(long shopId);
    MerchantSalesStatisticsResponse sales(long shopId, String dateType, String startDate, String endDate);
    MerchantProductStatisticsResponse product(long shopId, String sortBy, Integer limit);
    MerchantCustomerStatisticsResponse customer(long shopId);
}

package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.merchant.order.*;
import com.lingnan.fruitshop.dto.merchant.order.vo.MerchantOrderDetailResponse;
import com.lingnan.fruitshop.dto.merchant.order.vo.MerchantOrderListResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface MerchantOrderService {
    MerchantOrderListResponse list(long shopId, int page, int pageSize, Integer status, String keyword, String startTime, String endTime);
    MerchantOrderDetailResponse detail(long shopId, long orderId);
    void deliver(long shopId, MerchantOrderDeliverRequest req);
    void batchDeliver(long shopId, MerchantOrderBatchDeliverRequest req);
    void updateShippingFee(long shopId, MerchantOrderUpdateShippingFeeRequest req);
    void updateRemark(long shopId, MerchantOrderUpdateRemarkRequest req);
    Map<String, Object> export(long shopId, MerchantOrderExportRequest req);
}

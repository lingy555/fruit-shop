package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.merchant.refund.*;
import com.lingnan.fruitshop.dto.merchant.refund.vo.MerchantRefundDetailResponse;
import com.lingnan.fruitshop.dto.merchant.refund.vo.MerchantRefundListResponse;

import java.time.LocalDateTime;

public interface MerchantRefundService {
    MerchantRefundListResponse list(long shopId, int page, int pageSize, Integer status, String keyword);
    MerchantRefundDetailResponse detail(long shopId, long refundId);
    void agree(long shopId, MerchantRefundAgreeRequest req);
    void reject(long shopId, MerchantRefundRejectRequest req);
    void confirmReceive(long shopId, MerchantRefundConfirmReceiveRequest req);
}

package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.refund.AdminRefundArbitrateRequest;
import com.lingnan.fruitshop.dto.admin.refund.vo.AdminRefundDetailResponse;
import com.lingnan.fruitshop.dto.admin.refund.vo.AdminRefundListResponse;
import java.time.LocalDateTime;

public interface AdminRefundService {
    AdminRefundListResponse list(int page, int pageSize, String refundNo, Integer status, Long shopId, String keyword);
    AdminRefundDetailResponse detail(long refundId);
    void arbitrate(AdminRefundArbitrateRequest req);
}

package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.notice.AdminNoticeAddRequest;
import com.lingnan.fruitshop.dto.admin.notice.AdminNoticeUpdateRequest;
import com.lingnan.fruitshop.dto.admin.notice.vo.AdminNoticeDetailResponse;
import com.lingnan.fruitshop.dto.admin.notice.vo.AdminNoticeListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminNoticeService {
    PageResponse<AdminNoticeListItemResponse> list(int page, int pageSize, Integer type, Integer status);
    AdminNoticeDetailResponse detail(long noticeId);
    void add(AdminNoticeAddRequest req);
    void update(long noticeId, AdminNoticeUpdateRequest req);
    void delete(long noticeId);
}

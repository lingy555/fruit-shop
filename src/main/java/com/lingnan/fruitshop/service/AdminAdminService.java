package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.admin.AdminAddRequest;
import com.lingnan.fruitshop.dto.admin.admin.AdminResetPasswordRequest;
import com.lingnan.fruitshop.dto.admin.admin.AdminUpdateRequest;
import com.lingnan.fruitshop.dto.admin.admin.vo.AdminListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import java.time.LocalDateTime;

public interface AdminAdminService {
    PageResponse<AdminListItemResponse> list(int page, int pageSize, String keyword, Long roleId, Integer status);
    void add(AdminAddRequest req);
    void update(long adminId, AdminUpdateRequest req);
    void delete(long adminId);
    void resetPassword(long adminId, AdminResetPasswordRequest req);
}

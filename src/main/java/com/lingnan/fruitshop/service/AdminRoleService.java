package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.role.RoleAddRequest;
import com.lingnan.fruitshop.dto.admin.role.RoleUpdateRequest;
import com.lingnan.fruitshop.dto.admin.role.vo.RoleDetailResponse;
import com.lingnan.fruitshop.dto.admin.role.vo.RoleListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminRoleService {
    PageResponse<RoleListItemResponse> list(int page, int pageSize, String keyword);
    RoleDetailResponse detail(long roleId);
    void add(RoleAddRequest req);
    void update(long roleId, RoleUpdateRequest req);
    void delete(long roleId);
    List<Long> permissions(long roleId);
}

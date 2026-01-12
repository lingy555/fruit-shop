package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.permission.vo.PermissionTreeNodeResponse;

import java.util.List;

public interface AdminPermissionService {
    List<PermissionTreeNodeResponse> tree();
}

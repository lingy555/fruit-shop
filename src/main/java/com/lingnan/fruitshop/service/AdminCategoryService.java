package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.category.AdminCategoryAddRequest;
import com.lingnan.fruitshop.dto.admin.category.AdminCategoryUpdateRequest;
import com.lingnan.fruitshop.dto.admin.category.vo.AdminCategoryTreeNodeResponse;

import java.util.List;

public interface AdminCategoryService {
    List<AdminCategoryTreeNodeResponse> tree();
    void add(AdminCategoryAddRequest req);
    void update(long categoryId, AdminCategoryUpdateRequest req);
    void delete(long categoryId);
}

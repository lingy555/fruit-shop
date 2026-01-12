package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.category.AdminCategoryAddRequest;
import com.lingnan.fruitshop.dto.admin.category.AdminCategoryUpdateRequest;
import com.lingnan.fruitshop.dto.admin.category.vo.AdminCategoryTreeNodeResponse;
import com.lingnan.fruitshop.service.AdminCategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<AdminCategoryTreeNodeResponse>> tree() {
        return ApiResponse.success(adminCategoryService.tree());
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody AdminCategoryAddRequest req) {
        adminCategoryService.add(req);
        return ApiResponse.success(null);
    }

    @PutMapping("/update/{categoryId}")
    public ApiResponse<Void> update(@PathVariable long categoryId, @Valid @RequestBody AdminCategoryUpdateRequest req) {
        adminCategoryService.update(categoryId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{categoryId}")
    public ApiResponse<Void> delete(@PathVariable long categoryId) {
        adminCategoryService.delete(categoryId);
        return ApiResponse.success(null);
    }
}

package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.role.RoleAddRequest;
import com.lingnan.fruitshop.dto.admin.role.RoleUpdateRequest;
import com.lingnan.fruitshop.dto.admin.role.vo.RoleDetailResponse;
import com.lingnan.fruitshop.dto.admin.role.vo.RoleListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.service.AdminRoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/role")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    public AdminRoleController(AdminRoleService adminRoleService) {
        this.adminRoleService = adminRoleService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<RoleListItemResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "20") int pageSize,
                                                                @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminRoleService.list(page, pageSize, keyword));
    }

    @GetMapping("/detail/{roleId}")
    public ApiResponse<RoleDetailResponse> detail(@PathVariable long roleId) {
        return ApiResponse.success(adminRoleService.detail(roleId));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody RoleAddRequest req) {
        adminRoleService.add(req);
        return ApiResponse.success(null);
    }

    @PutMapping("/update/{roleId}")
    public ApiResponse<Void> update(@PathVariable long roleId, @Valid @RequestBody RoleUpdateRequest req) {
        adminRoleService.update(roleId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{roleId}")
    public ApiResponse<Void> delete(@PathVariable long roleId) {
        adminRoleService.delete(roleId);
        return ApiResponse.success(null);
    }

    @GetMapping("/permissions/{roleId}")
    public ApiResponse<List<Long>> permissions(@PathVariable long roleId) {
        return ApiResponse.success(adminRoleService.permissions(roleId));
    }
}

package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.admin.AdminAddRequest;
import com.lingnan.fruitshop.dto.admin.admin.AdminResetPasswordRequest;
import com.lingnan.fruitshop.dto.admin.admin.AdminUpdateRequest;
import com.lingnan.fruitshop.dto.admin.admin.vo.AdminListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.service.AdminAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/admin")
public class AdminAdminController {

    private final AdminAdminService adminAdminService;

    public AdminAdminController(AdminAdminService adminAdminService) {
        this.adminAdminService = adminAdminService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<AdminListItemResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int pageSize,
                                                                 @RequestParam(required = false) String keyword,
                                                                 @RequestParam(required = false) Long roleId,
                                                                 @RequestParam(required = false) Integer status) {
        return ApiResponse.success(adminAdminService.list(page, pageSize, keyword, roleId, status));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody AdminAddRequest req) {
        adminAdminService.add(req);
        return ApiResponse.success(null);
    }

    @PutMapping("/update/{adminId}")
    public ApiResponse<Void> update(@PathVariable long adminId, @Valid @RequestBody AdminUpdateRequest req) {
        adminAdminService.update(adminId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{adminId}")
    public ApiResponse<Void> delete(@PathVariable long adminId) {
        adminAdminService.delete(adminId);
        return ApiResponse.success(null);
    }

    @PutMapping("/resetPassword/{adminId}")
    public ApiResponse<Void> resetPassword(@PathVariable long adminId, @Valid @RequestBody AdminResetPasswordRequest req) {
        adminAdminService.resetPassword(adminId, req);
        return ApiResponse.success(null);
    }
}

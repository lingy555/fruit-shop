package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.banner.AdminBannerAddRequest;
import com.lingnan.fruitshop.dto.admin.banner.AdminBannerUpdateRequest;
import com.lingnan.fruitshop.dto.admin.banner.vo.AdminBannerItemResponse;
import com.lingnan.fruitshop.service.AdminBannerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banner")
public class AdminBannerController {

    private final AdminBannerService adminBannerService;

    public AdminBannerController(AdminBannerService adminBannerService) {
        this.adminBannerService = adminBannerService;
    }

    @GetMapping("/list")
    public ApiResponse<List<AdminBannerItemResponse>> list() {
        return ApiResponse.success(adminBannerService.list());
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody AdminBannerAddRequest req) {
        adminBannerService.add(req);
        return ApiResponse.success(null);
    }

    @PutMapping("/update/{bannerId}")
    public ApiResponse<Void> update(@PathVariable long bannerId, @Valid @RequestBody AdminBannerUpdateRequest req) {
        adminBannerService.update(bannerId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{bannerId}")
    public ApiResponse<Void> delete(@PathVariable long bannerId) {
        adminBannerService.delete(bannerId);
        return ApiResponse.success(null);
    }
}

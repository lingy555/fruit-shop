package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.notice.AdminNoticeAddRequest;
import com.lingnan.fruitshop.dto.admin.notice.AdminNoticeUpdateRequest;
import com.lingnan.fruitshop.dto.admin.notice.vo.AdminNoticeDetailResponse;
import com.lingnan.fruitshop.dto.admin.notice.vo.AdminNoticeListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.service.AdminNoticeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notice")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    public AdminNoticeController(AdminNoticeService adminNoticeService) {
        this.adminNoticeService = adminNoticeService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<AdminNoticeListItemResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                                       @RequestParam(required = false) Integer type,
                                                                       @RequestParam(required = false) Integer status) {
        return ApiResponse.success(adminNoticeService.list(page, pageSize, type, status));
    }

    @GetMapping("/detail/{noticeId}")
    public ApiResponse<AdminNoticeDetailResponse> detail(@PathVariable long noticeId) {
        return ApiResponse.success(adminNoticeService.detail(noticeId));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody AdminNoticeAddRequest req) {
        adminNoticeService.add(req);
        return ApiResponse.success(null);
    }

    @PutMapping("/update/{noticeId}")
    public ApiResponse<Void> update(@PathVariable long noticeId, @Valid @RequestBody AdminNoticeUpdateRequest req) {
        adminNoticeService.update(noticeId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{noticeId}")
    public ApiResponse<Void> delete(@PathVariable long noticeId) {
        adminNoticeService.delete(noticeId);
        return ApiResponse.success(null);
    }
}

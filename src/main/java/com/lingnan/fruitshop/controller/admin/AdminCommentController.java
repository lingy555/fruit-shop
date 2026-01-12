package com.lingnan.fruitshop.controller.admin;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.admin.comment.AdminCommentUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.comment.vo.AdminCommentListResponse;
import com.lingnan.fruitshop.service.AdminCommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    public AdminCommentController(AdminCommentService adminCommentService) {
        this.adminCommentService = adminCommentService;
    }

    @GetMapping("/list")
    public ApiResponse<AdminCommentListResponse> list(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize,
                                                      @RequestParam(required = false) Long productId,
                                                      @RequestParam(required = false) Long shopId,
                                                      @RequestParam(required = false) Integer score,
                                                      @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminCommentService.list(page, pageSize, productId, shopId, score, keyword));
    }

    @DeleteMapping("/delete/{commentId}")
    public ApiResponse<Void> delete(@PathVariable long commentId) {
        adminCommentService.delete(commentId);
        return ApiResponse.success(null);
    }

    @PutMapping("/updateStatus")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody AdminCommentUpdateStatusRequest req) {
        adminCommentService.updateStatus(req);
        return ApiResponse.success(null);
    }
}

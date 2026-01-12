package com.lingnan.fruitshop.controller.merchant;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.merchant.comment.MerchantCommentReplyRequest;
import com.lingnan.fruitshop.dto.merchant.comment.vo.MerchantCommentListResponse;
import com.lingnan.fruitshop.security.MerchantSecurityUtils;
import com.lingnan.fruitshop.service.MerchantCommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/comment")
public class MerchantCommentController {

    private final MerchantCommentService merchantCommentService;

    public MerchantCommentController(MerchantCommentService merchantCommentService) {
        this.merchantCommentService = merchantCommentService;
    }

    @GetMapping("/list")
    public ApiResponse<MerchantCommentListResponse> list(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                         @RequestParam(required = false) Long productId,
                                                         @RequestParam(required = false) Integer score,
                                                         @RequestParam(required = false) Boolean hasReply,
                                                         @RequestParam(required = false) String keyword) {
        long shopId = MerchantSecurityUtils.currentShopId();
        return ApiResponse.success(merchantCommentService.list(shopId, page, pageSize, productId, score, hasReply, keyword));
    }

    @PostMapping("/reply")
    public ApiResponse<Void> reply(@Valid @RequestBody MerchantCommentReplyRequest req) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantCommentService.reply(shopId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/deleteReply/{commentId}")
    public ApiResponse<Void> deleteReply(@PathVariable long commentId) {
        long shopId = MerchantSecurityUtils.currentShopId();
        merchantCommentService.deleteReply(shopId, commentId);
        return ApiResponse.success(null);
    }
}

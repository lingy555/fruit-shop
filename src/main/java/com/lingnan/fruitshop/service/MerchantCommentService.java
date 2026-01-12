package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.merchant.comment.MerchantCommentReplyRequest;
import com.lingnan.fruitshop.dto.merchant.comment.vo.MerchantCommentListResponse;
import java.time.LocalDateTime;

public interface MerchantCommentService {
    MerchantCommentListResponse list(long shopId, int page, int pageSize, Long productId, Integer score, Boolean hasReply, String keyword);
    void reply(long shopId, MerchantCommentReplyRequest req);
    void deleteReply(long shopId, long commentId);
}

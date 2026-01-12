package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.comment.AdminCommentUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.comment.vo.AdminCommentListResponse;
import java.time.LocalDateTime;

public interface AdminCommentService {
    AdminCommentListResponse list(int page, int pageSize, Long productId, Long shopId, Integer score, String keyword);
    void delete(long commentId);
    void updateStatus(AdminCommentUpdateStatusRequest req);
}

package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.comment.CommentAddRequest;
import com.lingnan.fruitshop.dto.customer.comment.CommentAppendRequest;
import com.lingnan.fruitshop.dto.customer.comment.vo.MyCommentItemResponse;
import com.lingnan.fruitshop.dto.customer.comment.vo.WaitCommentItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    PageResponse<WaitCommentItemResponse> waitList(long userId, int page, int pageSize);
    void add(long userId, CommentAddRequest req);
    void append(long userId, CommentAppendRequest req);
    PageResponse<MyCommentItemResponse> myList(long userId, int page, int pageSize);
    void delete(long userId, long commentId);
}

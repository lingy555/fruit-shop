package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.comment.CommentAddRequest;
import com.lingnan.fruitshop.dto.customer.comment.CommentAppendRequest;
import com.lingnan.fruitshop.dto.customer.comment.vo.MyCommentItemResponse;
import com.lingnan.fruitshop.dto.customer.comment.vo.WaitCommentItemResponse;
import com.lingnan.fruitshop.security.SecurityUtils;
import com.lingnan.fruitshop.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/waitList")
    public ApiResponse<PageResponse<WaitCommentItemResponse>> waitList(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "10") int pageSize) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(commentService.waitList(userId, page, pageSize));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody CommentAddRequest req) {
        long userId = SecurityUtils.currentUserId();
        commentService.add(userId, req);
        return ApiResponse.success(null);
    }

    @PostMapping("/append")
    public ApiResponse<Void> append(@Valid @RequestBody CommentAppendRequest req) {
        long userId = SecurityUtils.currentUserId();
        commentService.append(userId, req);
        return ApiResponse.success(null);
    }

    @GetMapping("/myList")
    public ApiResponse<PageResponse<MyCommentItemResponse>> myList(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(commentService.myList(userId, page, pageSize));
    }

    @DeleteMapping("/delete/{commentId}")
    public ApiResponse<Void> delete(@PathVariable long commentId) {
        long userId = SecurityUtils.currentUserId();
        commentService.delete(userId, commentId);
        return ApiResponse.success(null);
    }
}

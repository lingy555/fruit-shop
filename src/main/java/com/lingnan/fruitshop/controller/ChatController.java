package com.lingnan.fruitshop.controller;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.chat.ChatMessageResponse;
import com.lingnan.fruitshop.dto.chat.ChatSessionResponse;
import com.lingnan.fruitshop.dto.chat.SendMessageRequest;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.service.ChatService;
import com.lingnan.fruitshop.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取用户的聊天会话列表
     */
    @GetMapping("/sessions/user")
    public ApiResponse<PageResponse<ChatSessionResponse>> getUserSessions(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResponse<ChatSessionResponse> sessions = chatService.getUserSessions(userId, page, pageSize);
        return ApiResponse.success(sessions);
    }

    /**
     * 获取商家的聊天会话列表
     */
    @GetMapping("/sessions/merchant")
    public ApiResponse<PageResponse<ChatSessionResponse>> getMerchantSessions(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResponse<ChatSessionResponse> sessions = chatService.getMerchantSessions(merchantId, page, pageSize);
        return ApiResponse.success(sessions);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/session/{sessionId}")
    public ApiResponse<ChatSessionResponse> getSessionDetail(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long merchantId) {
        ChatSessionResponse session = chatService.getSessionDetail(sessionId, userId, merchantId);
        return ApiResponse.success(session);
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/messages/{sessionId}")
    public ApiResponse<PageResponse<ChatMessageResponse>> getSessionMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long merchantId) {
        PageResponse<ChatMessageResponse> messages = chatService.getSessionMessages(sessionId, page, pageSize, userId, merchantId);
        return ApiResponse.success(messages);
    }

    /**
     * 发送消息
     */
    @PostMapping("/message/send")
    public ApiResponse<ChatMessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        ChatMessageResponse message = chatService.sendMessage(request);
        return ApiResponse.success(message);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/messages/read")
    public ApiResponse<Void> markMessagesAsRead(
            @RequestParam Long sessionId,
            @RequestParam Integer readerType,
            @RequestParam Long readerId) {
        chatService.markMessagesAsRead(sessionId, readerType, readerId);
        return ApiResponse.success(null);
    }

    /**
     * 创建或获取会话
     */
    @PostMapping("/session/create")
    public ApiResponse<ChatSessionResponse> createOrGetSession(
            @RequestParam Long userId,
            @RequestParam Long merchantId,
            @RequestParam Long shopId) {
        ChatSessionResponse session = chatService.createOrGetSession(userId, merchantId, shopId);
        return ApiResponse.success(session);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public ApiResponse<Integer> getUnreadCount(
            @RequestParam Long userId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam Integer userType) {
        Integer count = chatService.getUnreadCount(userId, merchantId, userType);
        return ApiResponse.success(count);
    }
}

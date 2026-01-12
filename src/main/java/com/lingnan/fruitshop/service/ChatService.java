package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.chat.ChatMessageResponse;
import com.lingnan.fruitshop.dto.chat.ChatSessionResponse;
import com.lingnan.fruitshop.dto.chat.SendMessageRequest;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;

public interface ChatService {

    /**
     * 获取用户的聊天会话列表
     */
    PageResponse<ChatSessionResponse> getUserSessions(Long userId, int page, int pageSize);

    /**
     * 获取商家的聊天会话列表
     */
    PageResponse<ChatSessionResponse> getMerchantSessions(Long merchantId, int page, int pageSize);

    /**
     * 获取会话详情
     */
    ChatSessionResponse getSessionDetail(Long sessionId, Long userId, Long merchantId);

    /**
     * 获取会话消息列表
     */
    PageResponse<ChatMessageResponse> getSessionMessages(Long sessionId, int page, int pageSize, Long userId, Long merchantId);

    /**
     * 发送消息
     */
    ChatMessageResponse sendMessage(SendMessageRequest request);

    /**
     * 标记消息为已读
     */
    void markMessagesAsRead(Long sessionId, Integer readerType, Long readerId);

    /**
     * 创建或获取会话
     */
    ChatSessionResponse createOrGetSession(Long userId, Long merchantId, Long shopId);

    /**
     * 获取未读消息数量
     */
    Integer getUnreadCount(Long userId, Long merchantId, Integer userType);
}

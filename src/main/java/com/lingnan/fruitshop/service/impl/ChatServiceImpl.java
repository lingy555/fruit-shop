package com.lingnan.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.dto.chat.ChatMessageResponse;
import com.lingnan.fruitshop.dto.chat.ChatSessionResponse;
import com.lingnan.fruitshop.dto.chat.SendMessageRequest;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import com.lingnan.fruitshop.service.ChatService;
import com.lingnan.fruitshop.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserChatRelationMapper userChatRelationMapper;
    private final UserAccountMapper userAccountMapper;
    private final MerchantAccountMapper merchantAccountMapper;

    @Override
    public PageResponse<ChatSessionResponse> getUserSessions(Long userId, int page, int pageSize) {
        Page<ChatSession> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getLastMessageTime);

        Page<ChatSession> result = chatSessionMapper.selectPage(pageParam, qw);
        
        List<ChatSessionResponse> responses = result.getRecords().stream()
                .map(this::convertToSessionResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(result.getTotal(), responses);
    }

    @Override
    public PageResponse<ChatSessionResponse> getMerchantSessions(Long merchantId, int page, int pageSize) {
        Page<ChatSession> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<ChatSession>()
                // 查询所有会话，不根据merchantId过滤
                .orderByDesc(ChatSession::getLastMessageTime);

        Page<ChatSession> result = chatSessionMapper.selectPage(pageParam, qw);
        
        List<ChatSessionResponse> responses = result.getRecords().stream()
                .map(this::convertToSessionResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(result.getTotal(), responses);
    }

    @Override
    public ChatSessionResponse getSessionDetail(Long sessionId, Long userId, Long merchantId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.notFound("会话不存在");
        }

        // 验证权限
        if (userId != null && !session.getUserId().equals(userId)) {
            throw BizException.forbidden("无权访问此会话");
        }
        // 商家端可以访问所有会话，不再验证merchantId
        // if (merchantId != null && !session.getMerchantId().equals(merchantId)) {
        //     throw BizException.forbidden("无权访问此会话");
        // }

        return convertToSessionResponse(session);
    }

    @Override
    public PageResponse<ChatMessageResponse> getSessionMessages(Long sessionId, int page, int pageSize, Long userId, Long merchantId) {
        // 验证会话权限
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.notFound("会话不存在");
        }

        if (userId != null && !session.getUserId().equals(userId)) {
            throw BizException.forbidden("无权访问此会话");
        }
        // 商家端可以访问所有会话，不再验证merchantId
        // if (merchantId != null && !session.getMerchantId().equals(merchantId)) {
        //     throw BizException.forbidden("无权访问此会话");
        // }

        Page<ChatMessage> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ChatMessage> qw = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getSendTime);

        Page<ChatMessage> result = chatMessageMapper.selectPage(pageParam, qw);
        
        List<ChatMessageResponse> responses = result.getRecords().stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(result.getTotal(), responses);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(SendMessageRequest request) {
        System.out.println("收到发送消息请求: " + request);
        
        // 验证会话
        ChatSession session = chatSessionMapper.selectById(request.getSessionId());
        if (session == null) {
            throw BizException.notFound("会话不存在");
        }
        
        System.out.println("会话验证通过: " + session.getSessionId());

        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setSessionId(request.getSessionId());
        message.setSenderType(request.getSenderType());
        message.setSenderId(request.getSenderId());
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setExtraData(request.getExtraData());
        message.setIsRead(0);
        message.setSendTime(LocalDateTime.now());

        chatMessageMapper.insert(message);
        System.out.println("消息插入成功: " + message.getMessageId());

        // 更新会话信息
        chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                .set(ChatSession::getLastMessageId, message.getMessageId())
                .set(ChatSession::getLastMessageTime, message.getSendTime())
                .set(ChatSession::getLastMessageContent, request.getContent())
                .eq(ChatSession::getSessionId, request.getSessionId()));

        // 更新未读计数
        if (request.getSenderType() == 1) { // 用户发送
            chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                    .set(ChatSession::getMerchantUnreadCount, 
                            session.getMerchantUnreadCount() + 1)
                    .eq(ChatSession::getSessionId, request.getSessionId()));
        } else { // 商家发送
            chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                    .set(ChatSession::getUserUnreadCount, 
                            session.getUserUnreadCount() + 1)
                    .eq(ChatSession::getSessionId, request.getSessionId()));
        }

        // 更新关系表的最后联系时间
        userChatRelationMapper.update(null, new LambdaUpdateWrapper<UserChatRelation>()
                .set(UserChatRelation::getLastContactTime, LocalDateTime.now())
                .eq(UserChatRelation::getSessionId, request.getSessionId()));

        return convertToMessageResponse(message);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long sessionId, Integer readerType, Long readerId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.notFound("会话不存在");
        }

        // 标记消息为已读
        chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .set(ChatMessage::getIsRead, 1)
                .set(ChatMessage::getReadTime, LocalDateTime.now())
                .eq(ChatMessage::getSessionId, sessionId)
                .ne(ChatMessage::getSenderType, readerType) // 只标记对方发送的消息
                .eq(ChatMessage::getIsRead, 0));

        // 更新会话未读计数
        if (readerType == 1) { // 用户读取
            chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                    .set(ChatSession::getUserUnreadCount, 0)
                    .eq(ChatSession::getSessionId, sessionId));
        } else { // 商家读取
            chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                    .set(ChatSession::getMerchantUnreadCount, 0)
                    .eq(ChatSession::getSessionId, sessionId));
        }
    }

    @Override
    @Transactional
    public ChatSessionResponse createOrGetSession(Long userId, Long merchantId, Long shopId) {
        // 查找现有会话（根据userId和shopId，不根据merchantId）
        LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getShopId, shopId);

        ChatSession existingSession = chatSessionMapper.selectOne(qw);
        if (existingSession != null) {
            return convertToSessionResponse(existingSession);
        }

        // 创建新会话
        ChatSession newSession = new ChatSession();
        newSession.setUserId(userId);
        newSession.setMerchantId(merchantId);
        newSession.setShopId(shopId);
        newSession.setUserUnreadCount(0);
        newSession.setMerchantUnreadCount(0);
        newSession.setStatus(1);

        chatSessionMapper.insert(newSession);

        // 创建关系记录
        UserChatRelation relation = new UserChatRelation();
        relation.setUserId(userId);
        relation.setMerchantId(merchantId);
        relation.setShopId(shopId);
        relation.setSessionId(newSession.getSessionId());
        relation.setLastContactTime(LocalDateTime.now());

        userChatRelationMapper.insert(relation);

        return convertToSessionResponse(newSession);
    }

    @Override
    public Integer getUnreadCount(Long userId, Long merchantId, Integer userType) {
        if (userType == 1) { // 用户
            LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<ChatSession>()
                    .eq(ChatSession::getUserId, userId);
            if (merchantId != null) {
                qw.eq(ChatSession::getMerchantId, merchantId);
            }
            
            List<ChatSession> sessions = chatSessionMapper.selectList(qw);
            return sessions.stream().mapToInt(ChatSession::getUserUnreadCount).sum();
        } else { // 商家
            LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<ChatSession>()
                    .eq(ChatSession::getMerchantId, merchantId);
            
            List<ChatSession> sessions = chatSessionMapper.selectList(qw);
            return sessions.stream().mapToInt(ChatSession::getMerchantUnreadCount).sum();
        }
    }

    private ChatSessionResponse convertToSessionResponse(ChatSession session) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setSessionId(session.getSessionId());
        response.setUserId(session.getUserId());
        response.setMerchantId(session.getMerchantId());
        response.setShopId(session.getShopId());
        response.setLastMessageId(session.getLastMessageId());
        response.setLastMessageTime(session.getLastMessageTime());
        response.setLastMessageContent(session.getLastMessageContent());
        response.setUserUnreadCount(session.getUserUnreadCount());
        response.setMerchantUnreadCount(session.getMerchantUnreadCount());
        response.setStatus(session.getStatus());
        response.setCreateTime(session.getCreateTime());
        response.setUpdateTime(session.getUpdateTime());

        // 获取用户信息
        UserAccount user = userAccountMapper.selectById(session.getUserId());
        if (user != null) {
            response.setUserName(user.getNickname());
            response.setUserAvatar(user.getAvatar());
        }

        // 获取商家信息
        MerchantAccount merchant = merchantAccountMapper.selectById(session.getMerchantId());
        if (merchant != null) {
            response.setShopName(merchant.getShopName());
        }

        return response;
    }

    private ChatMessageResponse convertToMessageResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setMessageId(message.getMessageId());
        response.setSessionId(message.getSessionId());
        response.setSenderType(message.getSenderType());
        response.setSenderId(message.getSenderId());
        response.setMessageType(message.getMessageType());
        response.setContent(message.getContent());
        response.setExtraData(message.getExtraData());
        response.setIsRead(message.getIsRead());
        response.setSendTime(message.getSendTime());
        response.setReadTime(message.getReadTime());

        // 获取发送者信息
        if (message.getSenderType() == 1) { // 用户
            UserAccount user = userAccountMapper.selectById(message.getSenderId());
            if (user != null) {
                response.setSenderName(user.getNickname());
                response.setSenderAvatar(user.getAvatar());
            }
        } else { // 商家
            MerchantAccount merchant = merchantAccountMapper.selectById(message.getSenderId());
            if (merchant != null) {
                response.setSenderName(merchant.getShopName());
                response.setSenderAvatar(null); // 暂时设为null，后续可以添加logo字段
            }
        }

        return response;
    }
}

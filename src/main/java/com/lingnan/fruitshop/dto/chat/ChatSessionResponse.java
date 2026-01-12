package com.lingnan.fruitshop.dto.chat;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatSessionResponse {

    private Long sessionId;
    private Long userId;
    private Long merchantId;
    private Long shopId;
    private String shopName;
    private String shopLogo;
    private String userName;
    private String userAvatar;
    private Long lastMessageId;
    private LocalDateTime lastMessageTime;
    private String lastMessageContent;
    private Integer userUnreadCount;
    private Integer merchantUnreadCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 最近的消息列表（用于会话详情）
    private List<ChatMessageResponse> recentMessages;
}

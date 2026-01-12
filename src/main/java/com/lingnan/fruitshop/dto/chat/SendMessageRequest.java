package com.lingnan.fruitshop.dto.chat;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class SendMessageRequest {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @NotNull(message = "发送者类型不能为空")
    private Integer senderType; // 1-用户，2-商家

    @NotNull(message = "发送者ID不能为空")
    private Long senderId;

    @NotNull(message = "消息类型不能为空")
    private Integer messageType; // 1-文本，2-图片，3-商品

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private String extraData; // JSON格式的额外数据
}

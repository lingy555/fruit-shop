package com.lingnan.fruitshop.service;

public interface SmsSender {

    /**
     * 发送短信内容到指定手机号
     *
     * @param phone   目标手机号
     * @param content 短信内容
     */
    void send(String phone, String content);
}

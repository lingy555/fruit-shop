package com.lingnan.fruitshop.service;

public interface SmsCodeService {
    String sendCode(String phone, String type);
    
    boolean verifyCode(String phone, String type, String code);
}

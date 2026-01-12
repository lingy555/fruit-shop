package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.service.SmsCodeService;
import com.lingnan.fruitshop.service.SmsSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeServiceImpl implements SmsCodeService {

    private static final long EXPIRE_MILLIS = Duration.ofMinutes(5).toMillis();
    private static final long RESEND_INTERVAL = Duration.ofSeconds(60).toMillis();

    private final Map<String, CodeRecord> records = new ConcurrentHashMap<>();
    private final SmsSender smsSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public SmsCodeServiceImpl(SmsSender smsSender) {
        this.smsSender = smsSender;
    }

    @Override
    public String sendCode(String phone, String type) {
        String key = key(phone, type);
        CodeRecord previous = records.get(key);
        long now = System.currentTimeMillis();
        if (previous != null && now - previous.sendAt < RESEND_INTERVAL) {
            long remain = (RESEND_INTERVAL - (now - previous.sendAt)) / 1000;
            throw BizException.badRequest("发送过于频繁，请" + Math.max(remain, 1) + "秒后再试");
        }

        String code = generateCode();
        long expireAt = now + EXPIRE_MILLIS;
        records.put(key, new CodeRecord(code, expireAt, now));

        String content = String.format("【果蔬商城】您的验证码为 %s ，5分钟内有效。如非本人操作请忽略。", code);
        smsSender.send(phone, content);
        return code;
    }

    @Override
    public boolean verifyCode(String phone, String type, String code) {
        String key = key(phone, type);
        CodeRecord record = records.get(key);
        if (record == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now > record.expireAt) {
            records.remove(key);
            return false;
        }
        boolean match = record.code.equals(code);
        if (match) {
            records.remove(key);
        }
        return match;
    }

    private String key(String phone, String type) {
        return phone + ":" + type;
    }

    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private record CodeRecord(String code, long expireAt, long sendAt) {
    }
}

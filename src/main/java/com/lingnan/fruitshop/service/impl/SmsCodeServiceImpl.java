package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.SmsCodeService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeServiceImpl implements SmsCodeService {

    private static final long EXPIRE_MILLIS = 5 * 60 * 1000L;

    private final Map<String, CodeRecord> records = new ConcurrentHashMap<>();

    @Override
    public String sendCode(String phone, String type) {
        String code = "123456";
        records.put(key(phone, type), new CodeRecord(code, System.currentTimeMillis() + EXPIRE_MILLIS));
        return code;
    }

    @Override
    public boolean verifyCode(String phone, String type, String code) {
        CodeRecord record = records.get(key(phone, type));
        if (record == null) {
            return false;
        }
        if (System.currentTimeMillis() > record.expireAt) {
            records.remove(key(phone, type));
            return false;
        }
        return record.code.equals(code);
    }

    private String key(String phone, String type) {
        return phone + ":" + type;
    }

    private record CodeRecord(String code, long expireAt) {
    }
}

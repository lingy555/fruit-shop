package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LogSmsSender.class);

    @Override
    public void send(String phone, String content) {
        log.info("[SmsSender] phone={}, content={}", phone, content);
    }
}

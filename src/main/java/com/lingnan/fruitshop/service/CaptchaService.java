package com.lingnan.fruitshop.service;

import java.util.Map;

public interface CaptchaService {
    CaptchaResult generate();
    boolean verify(String captchaKey, String captcha);
    
    record CaptchaResult(String captchaKey, String captchaImage) {}
}

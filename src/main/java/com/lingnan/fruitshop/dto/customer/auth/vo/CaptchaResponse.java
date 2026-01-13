package com.lingnan.fruitshop.dto.customer.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CaptchaResponse {
    private String captchaKey;
    private String captchaImage;
}

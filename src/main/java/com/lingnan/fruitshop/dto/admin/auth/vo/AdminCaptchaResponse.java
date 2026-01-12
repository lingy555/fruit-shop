package com.lingnan.fruitshop.dto.admin.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminCaptchaResponse {

    private String captchaKey;
    private String captchaImage;
}

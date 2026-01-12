package com.lingnan.fruitshop.dto.admin.auth;

import lombok.Data;

@Data
public class AdminUpdateProfileRequest {

    private String nickname;

    private String avatar;

    private String phone;

    private String email;
}

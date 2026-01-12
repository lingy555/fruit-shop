package com.lingnan.fruitshop.dto.customer.user;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;

    private String avatar;

    private Integer gender;

    private String birthday;

    private String email;
}

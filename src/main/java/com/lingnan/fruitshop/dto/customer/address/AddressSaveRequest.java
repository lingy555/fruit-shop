package com.lingnan.fruitshop.dto.customer.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressSaveRequest {

    private Long addressId;

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    @NotNull(message = "是否默认不能为空")
    private Boolean isDefault;
}

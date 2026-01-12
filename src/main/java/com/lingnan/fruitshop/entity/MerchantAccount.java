package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchant_account")
public class MerchantAccount {

    @TableId(value = "merchant_id", type = IdType.AUTO)
    private Long merchantId;

    private Long applicationId;

    private Long shopId;

    private String shopName;

    private String contactName;

    private String contactPhone;

    private String email;

    private String businessLicense;

    private String idCardFront;

    private String idCardBack;

    private String passwordHash;

    private Integer status;

    private LocalDateTime auditTime;

    private String auditRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

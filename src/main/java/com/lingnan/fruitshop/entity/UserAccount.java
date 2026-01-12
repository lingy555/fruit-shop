package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_account")
public class UserAccount {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String username;

    private String passwordHash;

    private String phone;

    private String email;

    private String avatar;

    private String nickname;

    private Integer gender;

    private LocalDate birthday;

    private Integer level;

    private Integer points;

    private BigDecimal balance;

    private BigDecimal frozenBalance;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

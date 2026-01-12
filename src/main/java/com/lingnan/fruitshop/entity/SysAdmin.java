package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_admin")
public class SysAdmin {

    @TableId(value = "admin_id", type = IdType.AUTO)
    private Long adminId;

    private String username;

    private String passwordHash;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

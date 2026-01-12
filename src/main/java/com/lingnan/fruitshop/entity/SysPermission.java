package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;

    private Long parentId;

    private String permissionName;

    private String permissionCode;

    private String type;

    private String icon;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

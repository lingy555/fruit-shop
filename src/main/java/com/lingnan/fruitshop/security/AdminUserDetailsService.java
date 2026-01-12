package com.lingnan.fruitshop.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.entity.SysAdmin;
import com.lingnan.fruitshop.entity.SysAdminRole;
import com.lingnan.fruitshop.entity.SysPermission;
import com.lingnan.fruitshop.entity.SysRole;
import com.lingnan.fruitshop.entity.SysRolePermission;
import com.lingnan.fruitshop.mapper.SysAdminMapper;
import com.lingnan.fruitshop.mapper.SysAdminRoleMapper;
import com.lingnan.fruitshop.mapper.SysPermissionMapper;
import com.lingnan.fruitshop.mapper.SysRoleMapper;
import com.lingnan.fruitshop.mapper.SysRolePermissionMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final SysAdminMapper sysAdminMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public AdminUserDetailsService(SysAdminMapper sysAdminMapper,
                                  SysAdminRoleMapper sysAdminRoleMapper,
                                  SysRoleMapper sysRoleMapper,
                                  SysRolePermissionMapper sysRolePermissionMapper,
                                  SysPermissionMapper sysPermissionMapper) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysAdmin admin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, username)
                .last("limit 1"));
        if (admin == null) {
            throw new UsernameNotFoundException("管理员不存在");
        }
        return buildPrincipal(admin);
    }

    public UserDetails loadAdminById(long adminId) throws UsernameNotFoundException {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new UsernameNotFoundException("管理员不存在");
        }
        return buildPrincipal(admin);
    }

    private AdminPrincipal buildPrincipal(SysAdmin admin) {
        SysAdminRole adminRole = sysAdminRoleMapper.selectOne(new LambdaQueryWrapper<SysAdminRole>()
                .eq(SysAdminRole::getAdminId, admin.getAdminId())
                .last("limit 1"));

        Long roleId = adminRole == null ? null : adminRole.getRoleId();
        SysRole role = roleId == null ? null : sysRoleMapper.selectById(roleId);

        List<String> permissions;
        if (roleId == null) {
            permissions = Collections.emptyList();
        } else {
            List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                    .eq(SysRolePermission::getRoleId, roleId));
            List<Long> permissionIds = rolePermissions.stream().map(SysRolePermission::getPermissionId).filter(Objects::nonNull).distinct().toList();
            if (permissionIds.isEmpty()) {
                permissions = Collections.emptyList();
            } else {
                List<SysPermission> permissionList = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getPermissionId, permissionIds));
                permissions = permissionList.stream().map(SysPermission::getPermissionCode).filter(Objects::nonNull).distinct().toList();
            }
        }

        return new AdminPrincipal(admin,
                roleId,
                role == null ? null : role.getRoleName(),
                permissions);
    }
}

package com.lingnan.fruitshop.security;

import com.lingnan.fruitshop.entity.SysAdmin;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AdminPrincipal implements UserDetails {

    private final Long adminId;
    private final String username;
    private final String password;
    private final boolean enabled;

    private final Long roleId;
    private final String roleName;
    private final List<String> permissions;

    public AdminPrincipal(SysAdmin admin, Long roleId, String roleName, List<String> permissions) {
        this.adminId = admin.getAdminId();
        this.username = admin.getUsername();
        this.password = admin.getPasswordHash();
        this.enabled = admin.getStatus() != null && admin.getStatus() == 1;
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

package com.lingnan.fruitshop.security;

import com.lingnan.fruitshop.entity.UserAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomerPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final boolean enabled;

    public CustomerPrincipal(UserAccount userAccount) {
        this.userId = userAccount.getUserId();
        this.username = userAccount.getUsername();
        this.password = userAccount.getPasswordHash();
        this.enabled = userAccount.getStatus() != null && userAccount.getStatus() == 1;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
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

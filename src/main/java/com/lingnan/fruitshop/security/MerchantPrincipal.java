package com.lingnan.fruitshop.security;

import com.lingnan.fruitshop.entity.MerchantAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class MerchantPrincipal implements UserDetails {

    private final Long merchantId;
    private final Long shopId;
    private final String account;
    private final String password;
    private final boolean enabled;

    public MerchantPrincipal(MerchantAccount merchantAccount) {
        this.merchantId = merchantAccount.getMerchantId();
        this.shopId = merchantAccount.getShopId();
        this.account = merchantAccount.getContactPhone();
        this.password = merchantAccount.getPasswordHash();
        this.enabled = merchantAccount.getStatus() != null && merchantAccount.getStatus() == 1;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"));
    }

    @Override
    public String getUsername() {
        return account;
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

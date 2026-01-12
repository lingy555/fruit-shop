package com.lingnan.fruitshop.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.entity.MerchantAccount;
import com.lingnan.fruitshop.mapper.MerchantAccountMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MerchantUserDetailsService {

    private final MerchantAccountMapper merchantAccountMapper;

    public MerchantUserDetailsService(MerchantAccountMapper merchantAccountMapper) {
        this.merchantAccountMapper = merchantAccountMapper;
    }

    public UserDetails loadMerchantByAccount(String account) throws UsernameNotFoundException {
        MerchantAccount merchant = merchantAccountMapper.selectOne(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getContactPhone, account)
                .last("limit 1"));
        if (merchant == null) {
            throw new UsernameNotFoundException("商家不存在");
        }
        return new MerchantPrincipal(merchant);
    }

    public UserDetails loadMerchantById(long merchantId) throws UsernameNotFoundException {
        MerchantAccount merchant = merchantAccountMapper.selectById(merchantId);
        if (merchant == null) {
            throw new UsernameNotFoundException("商家不存在");
        }
        return new MerchantPrincipal(merchant);
    }
}

package com.lingnan.fruitshop.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.entity.UserAccount;
import com.lingnan.fruitshop.mapper.UserAccountMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final UserAccountMapper userAccountMapper;

    public CustomerUserDetailsService(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, username)
                .or()
                .eq(UserAccount::getPhone, username)
                .last("limit 1"));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new CustomerPrincipal(user);
    }

    public UserDetails loadUserById(long userId) throws UsernameNotFoundException {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new CustomerPrincipal(user);
    }
}

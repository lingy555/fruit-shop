package com.lingnan.fruitshop.test;

import com.lingnan.fruitshop.dto.customer.auth.LoginRequest;
import com.lingnan.fruitshop.dto.customer.auth.vo.LoginResponse;
import com.lingnan.fruitshop.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Token刷新功能测试
 */
@SpringBootTest
public class TokenRefreshTest {

    @Autowired
    private AuthService authService;

    @Test
    public void testTokenRefresh() {
        try {
            // 模拟用户登录获取token
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("testuser");
            loginRequest.setPassword("123456");
            loginRequest.setLoginType("password");
            
            LoginResponse loginResponse = authService.login(loginRequest);
            System.out.println("✅ 登录成功，获取到token: " + loginResponse.getToken().substring(0, 20) + "...");
            
            // 测试token刷新
            LoginResponse refreshResponse = authService.refreshToken(loginResponse.getUserInfo().getUserId());
            System.out.println("✅ Token刷新成功，新token: " + refreshResponse.getToken().substring(0, 20) + "...");
            
        } catch (Exception e) {
            System.out.println("❌ Token刷新测试失败: " + e.getMessage());
        }
    }
}

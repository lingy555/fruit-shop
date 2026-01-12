package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.auth.*;
import com.lingnan.fruitshop.dto.customer.auth.vo.LoginResponse;
import com.lingnan.fruitshop.dto.customer.auth.vo.RegisterResponse;
import java.math.BigDecimal;

public interface AuthService {
    RegisterResponse register(RegisterRequest req);
    
    LoginResponse login(LoginRequest req);
    
    LoginResponse loginByPhone(LoginByPhoneRequest req);
    
    void sendCode(SendCodeRequest req);
    
    void resetPassword(ResetPasswordRequest req);
    
    LoginResponse refreshToken(long userId);
}

package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.merchant.auth.*;
import com.lingnan.fruitshop.dto.merchant.auth.vo.MerchantLoginResponse;
import com.lingnan.fruitshop.dto.merchant.auth.vo.MerchantRegisterResponse;

public interface MerchantAuthService {
    MerchantRegisterResponse register(MerchantRegisterRequest req);
    
    MerchantLoginResponse login(MerchantLoginRequest req);
    
    MerchantLoginResponse getMerchantInfo(long merchantId);
    
    void changePassword(long merchantId, MerchantChangePasswordRequest req);
    
    void resetPassword(MerchantResetPasswordRequest req);
    
    MerchantLoginResponse refreshToken(long merchantId);
}

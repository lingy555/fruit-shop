package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.auth.AdminChangePasswordRequest;
import com.lingnan.fruitshop.dto.admin.auth.AdminLoginRequest;
import com.lingnan.fruitshop.dto.admin.auth.AdminUpdateProfileRequest;
import com.lingnan.fruitshop.dto.admin.auth.vo.AdminLoginResponse;
import java.time.LocalDateTime;

public interface AdminAuthService {
    AdminLoginResponse login(AdminLoginRequest req, String ip);
    
    AdminLoginResponse.AdminInfo currentInfo(long adminId);
    
    void changePassword(long adminId, AdminChangePasswordRequest req);
    
    void updateProfile(long adminId, AdminUpdateProfileRequest req);
}

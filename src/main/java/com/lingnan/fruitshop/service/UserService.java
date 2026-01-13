package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.user.*;
import com.lingnan.fruitshop.dto.customer.user.vo.BalanceResponse;
import com.lingnan.fruitshop.dto.customer.user.vo.PointsRecordsResponse;
import com.lingnan.fruitshop.dto.customer.user.vo.UserProfileResponse;
import java.math.BigDecimal;
public interface UserService {
    UserProfileResponse profile(long userId);
    
    void updateProfile(long userId, UpdateProfileRequest req);
    
    void changePassword(long userId, ChangePasswordRequest req);
    
    void changePhone(long userId, ChangePhoneRequest req);
    
    BalanceResponse balance(long userId);
    
    PointsRecordsResponse pointsRecords(long userId, int page, int pageSize);
    
    BalanceResponse recharge(long userId, BigDecimal amount);
}

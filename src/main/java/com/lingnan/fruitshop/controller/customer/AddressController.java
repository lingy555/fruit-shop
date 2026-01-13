package com.lingnan.fruitshop.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.address.AddressSaveRequest;
import com.lingnan.fruitshop.entity.UserAddress;
import com.lingnan.fruitshop.mapper.UserAddressMapper;
import com.lingnan.fruitshop.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customer/address")
public class AddressController {

    private final UserAddressMapper userAddressMapper;

    public AddressController(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }

    @GetMapping("/list")
    public ApiResponse<List<UserAddress>> list() {
        long userId = SecurityUtils.currentUserId();
        List<UserAddress> list = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime));
        return ApiResponse.success(list);
    }

    @PostMapping("/add")
    @Transactional
    public ApiResponse<Void> add(@Valid @RequestBody AddressSaveRequest req) {
        long userId = SecurityUtils.currentUserId();
        UserAddress ua = new UserAddress();
        ua.setUserId(userId);
        ua.setReceiverName(req.getReceiverName());
        ua.setReceiverPhone(req.getReceiverPhone());
        ua.setProvince(req.getProvince());
        ua.setCity(req.getCity());
        ua.setDistrict(req.getDistrict());
        ua.setDetail(req.getDetail());
        ua.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()) ? 1 : 0);
        ua.setCreateTime(LocalDateTime.now());
        ua.setUpdateTime(LocalDateTime.now());

        if (ua.getIsDefault() == 1) {
            // 取消其他默认
            userAddressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .eq(UserAddress::getIsDefault, 1)
                    .set(UserAddress::getIsDefault, 0));
        }

        userAddressMapper.insert(ua);
        return ApiResponse.success(null);
    }

    @PutMapping("/update")
    @Transactional
    public ApiResponse<Void> update(@Valid @RequestBody AddressSaveRequest req) {
        if (req.getAddressId() == null) {
            throw BizException.badRequest("地址ID不能为空");
        }
        long userId = SecurityUtils.currentUserId();
        UserAddress exists = userAddressMapper.selectById(req.getAddressId());
        if (exists == null || !exists.getUserId().equals(userId)) {
            throw BizException.notFound("地址不存在");
        }
        exists.setReceiverName(req.getReceiverName());
        exists.setReceiverPhone(req.getReceiverPhone());
        exists.setProvince(req.getProvince());
        exists.setCity(req.getCity());
        exists.setDistrict(req.getDistrict());
        exists.setDetail(req.getDetail());
        exists.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()) ? 1 : 0);
        exists.setUpdateTime(LocalDateTime.now());

        if (exists.getIsDefault() == 1) {
            userAddressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .eq(UserAddress::getIsDefault, 1)
                    .ne(UserAddress::getAddressId, exists.getAddressId())
                    .set(UserAddress::getIsDefault, 0));
        }

        userAddressMapper.updateById(exists);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{addressId}")
    public ApiResponse<Void> delete(@PathVariable Long addressId) {
        long userId = SecurityUtils.currentUserId();
        UserAddress exists = userAddressMapper.selectById(addressId);
        if (exists == null || !exists.getUserId().equals(userId)) {
            throw BizException.notFound("地址不存在");
        }
        userAddressMapper.deleteById(addressId);
        return ApiResponse.success(null);
    }

    @PutMapping("/setDefault/{addressId}")
    @Transactional
    public ApiResponse<Void> setDefault(@PathVariable Long addressId) {
        long userId = SecurityUtils.currentUserId();
        UserAddress exists = userAddressMapper.selectById(addressId);
        if (exists == null || !exists.getUserId().equals(userId)) {
            throw BizException.notFound("地址不存在");
        }
        // 清空原默认
        userAddressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0));

        exists.setIsDefault(1);
        exists.setUpdateTime(LocalDateTime.now());
        userAddressMapper.updateById(exists);
        return ApiResponse.success(null);
    }
}

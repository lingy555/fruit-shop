package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.cart.*;
import com.lingnan.fruitshop.dto.customer.cart.vo.CartAddResponse;
import com.lingnan.fruitshop.dto.customer.cart.vo.CartListResponse;
import com.lingnan.fruitshop.security.SecurityUtils;
import com.lingnan.fruitshop.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/list")
    public ApiResponse<CartListResponse> list() {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(cartService.list(userId));
    }

    @PostMapping("/add")
    public ApiResponse<CartAddResponse> add(@Valid @RequestBody CartAddRequest req) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success("添加成功", cartService.add(userId, req));
    }

    @PutMapping("/updateQuantity")
    public ApiResponse<Void> updateQuantity(@Valid @RequestBody CartUpdateQuantityRequest req) {
        long userId = SecurityUtils.currentUserId();
        cartService.updateQuantity(userId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody CartDeleteRequest req) {
        long userId = SecurityUtils.currentUserId();
        cartService.delete(userId, req);
        return ApiResponse.success(null);
    }

    @PutMapping("/selectAll")
    public ApiResponse<Void> selectAll(@Valid @RequestBody CartSelectAllRequest req) {
        long userId = SecurityUtils.currentUserId();
        cartService.selectAll(userId, Boolean.TRUE.equals(req.getSelected()));
        return ApiResponse.success(null);
    }

    @PutMapping("/select")
    public ApiResponse<Void> select(@Valid @RequestBody CartSelectRequest req) {
        long userId = SecurityUtils.currentUserId();
        cartService.select(userId, req);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/clearInvalid")
    public ApiResponse<Void> clearInvalid() {
        long userId = SecurityUtils.currentUserId();
        cartService.clearInvalid(userId);
        return ApiResponse.success(null);
    }
}

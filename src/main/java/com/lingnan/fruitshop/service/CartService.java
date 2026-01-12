package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.cart.CartAddRequest;
import com.lingnan.fruitshop.dto.customer.cart.CartDeleteRequest;
import com.lingnan.fruitshop.dto.customer.cart.CartSelectRequest;
import com.lingnan.fruitshop.dto.customer.cart.CartUpdateQuantityRequest;
import com.lingnan.fruitshop.dto.customer.cart.vo.CartAddResponse;
import com.lingnan.fruitshop.dto.customer.cart.vo.CartListResponse;
import java.math.BigDecimal;

public interface CartService {
    CartListResponse list(long userId);
    CartAddResponse add(long userId, CartAddRequest req);
    void updateQuantity(long userId, CartUpdateQuantityRequest req);
    void delete(long userId, CartDeleteRequest req);
    void selectAll(long userId, boolean selected);
    void select(long userId, CartSelectRequest req);
    void clearInvalid(long userId);
}

package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.order.*;
import com.lingnan.fruitshop.dto.customer.order.vo.*;

import java.math.BigDecimal;
import java.util.Map;

public interface OrderService {
    OrderConfirmResponse confirm(long userId, OrderConfirmRequest req);
    
    OrderCreateResponse create(long userId, OrderCreateRequest req);
    
    OrderDetailResponse detail(long userId, long orderId);
    
    void cancel(long userId, long orderId, OrderCancelRequest req);
    
    void confirmReceive(long userId, long orderId);
    
    void delete(long userId, long orderId);
    
    OrderPayResponse pay(long userId, OrderPayRequest req);
    
    Map<String, Object> payStatus(long userId, long orderId);
    
    void remindDeliver(long userId, long orderId);
    
    /**
     * 处理支付成功回调
     * @param orderId 订单ID
     * @param tradeNo 第三方交易号
     * @param amount 支付金额
     * @param payMethod 支付方式
     * @return 处理结果
     */
    boolean handlePaymentSuccess(long orderId, String tradeNo, BigDecimal amount, String payMethod);
    
    // 额外的方法
    PageResponse<OrderListResponse.OrderItem> list(long userId, int page, int pageSize, Integer status, String keyword);
    
    OrderListResponse.StatusCount statusCount(long userId);
    
    OrderDetailResponse.Logistics logistics(long userId, long orderId);
}

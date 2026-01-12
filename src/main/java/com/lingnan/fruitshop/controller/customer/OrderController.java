package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.order.*;
import com.lingnan.fruitshop.dto.customer.order.vo.*;
import com.lingnan.fruitshop.security.SecurityUtils;
import com.lingnan.fruitshop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/confirm")
    public ApiResponse<OrderConfirmResponse> confirm(@Valid @RequestBody OrderConfirmRequest req) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(orderService.confirm(userId, req));
    }

    @PostMapping("/create")
    public ApiResponse<OrderCreateResponse> create(@Valid @RequestBody OrderCreateRequest req) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success("订单创建成功", orderService.create(userId, req));
    }

    @GetMapping("/list")
    public ApiResponse<OrderListResponse> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false, defaultValue = "0") Integer status,
                                               @RequestParam(required = false) String keyword) {
        long userId = SecurityUtils.currentUserId();
        PageResponse<OrderListResponse.OrderItem> data = orderService.list(userId, page, pageSize, status, keyword);
        OrderListResponse.StatusCount statusCount = orderService.statusCount(userId);
        return ApiResponse.success(new OrderListResponse(data.getTotal(), statusCount, data.getList()));
    }

    @GetMapping("/detail/{orderId}")
    public ApiResponse<OrderDetailResponse> detail(@PathVariable long orderId) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(orderService.detail(userId, orderId));
    }

    @PutMapping("/cancel/{orderId}")
    public ApiResponse<Void> cancel(@PathVariable long orderId, @Valid @RequestBody OrderCancelRequest req) {
        long userId = SecurityUtils.currentUserId();
        orderService.cancel(userId, orderId, req);
        return ApiResponse.success(null);
    }

    @PutMapping("/confirm/{orderId}")
    public ApiResponse<Void> confirmReceive(@PathVariable long orderId) {
        long userId = SecurityUtils.currentUserId();
        orderService.confirmReceive(userId, orderId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/{orderId}")
    public ApiResponse<Void> delete(@PathVariable long orderId) {
        long userId = SecurityUtils.currentUserId();
        orderService.delete(userId, orderId);
        return ApiResponse.success(null);
    }

    @PostMapping("/pay")
    public ApiResponse<OrderPayResponse> pay(@Valid @RequestBody OrderPayRequest req) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(orderService.pay(userId, req));
    }

    @GetMapping("/payStatus/{orderId}")
    public ApiResponse<Map<String, Object>> payStatus(@PathVariable long orderId) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(orderService.payStatus(userId, orderId));
    }

    @GetMapping("/logistics/{orderId}")
    public ApiResponse<OrderDetailResponse.Logistics> logistics(@PathVariable long orderId) {
        long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(orderService.logistics(userId, orderId));
    }

    @PostMapping("/remindDeliver/{orderId}")
    public ApiResponse<Void> remindDeliver(@PathVariable long orderId) {
        long userId = SecurityUtils.currentUserId();
        orderService.remindDeliver(userId, orderId);
        return ApiResponse.success(null);
    }
}

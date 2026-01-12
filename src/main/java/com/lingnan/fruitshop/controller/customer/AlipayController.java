package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/alipay")
public class AlipayController {
    
    @Autowired
    private AlipayService alipayService;
    
    /**
     * 创建支付宝支付订单
     */
    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createPay(@RequestParam Long orderId,
                                                      @RequestParam BigDecimal amount,
                                                      @RequestParam(required = false) String subject,
                                                      @RequestParam(required = false) String body) {
        try {
            // 设置默认值
            if (subject == null) {
                subject = "水果商城订单-" + orderId;
            }
            if (body == null) {
                body = "订单号：" + orderId + "，金额：" + amount + "元";
            }
            
            // 创建支付表单
            String payForm = alipayService.createPagePay(orderId, amount.toString(), subject, body);
            
            Map<String, Object> result = new HashMap<>();
            result.put("payForm", payForm);
            result.put("orderId", orderId);
            result.put("amount", amount);
            
            return ApiResponse.success("创建支付订单成功", result);
            
        } catch (Exception e) {
            log.error("创建支付宝支付订单失败", e);
            return ApiResponse.fail(500, "创建支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询支付状态
     */
    @GetMapping("/query/{orderId}")
    public ApiResponse<Map<String, Object>> queryPayStatus(@PathVariable String orderId) {
        try {
            var response = alipayService.queryPayStatus(orderId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("tradeStatus", response.getTradeStatus());
            result.put("tradeNo", response.getTradeNo());
            result.put("totalAmount", response.getTotalAmount());
            result.put("message", response.getSubMsg());
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            return ApiResponse.fail(500, "查询支付状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 支付宝异步通知处理
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            // 获取所有参数
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    params.put(key, values[0]);
                }
            });
            
            log.info("收到支付宝异步通知: {}", params);
            
            // 处理通知
            return alipayService.handleNotify(params);
            
        } catch (Exception e) {
            log.error("处理支付宝异步通知异常", e);
            return "failure";
        }
    }
    
    /**
     * 支付宝同步回调处理
     */
    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 获取所有参数
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    params.put(key, values[0]);
                }
            });
            
            log.info("收到支付宝同步回调: {}", params);
            
            // 验证签名
            boolean verified = alipayService.verifyNotify(params);
            
            if (verified) {
                // 获取订单ID
                String orderId = params.get("out_trade_no");
                String tradeStatus = params.get("trade_status");
                
                log.info("支付宝回调验证成功: orderId={}, tradeStatus={}", orderId, tradeStatus);
                
                // 重定向到支付结果页面
                response.sendRedirect("/payment/return?orderId=" + orderId + "&payMethod=alipay");
            } else {
                log.error("支付宝回调签名验证失败");
                response.sendRedirect("/payment/fail?error=签名验证失败");
            }
            
        } catch (Exception e) {
            log.error("处理支付宝同步回调异常", e);
            response.sendRedirect("/payment/fail?error=回调处理异常");
        }
    }
    
    /**
     * 取消支付
     */
    @PostMapping("/cancel/{orderId}")
    public ApiResponse<String> cancelPay(@PathVariable String orderId) {
        try {
            var response = alipayService.closePay(orderId);
            
            if (response.isSuccess()) {
                return ApiResponse.success("取消支付成功");
            } else {
                return ApiResponse.fail(500, "取消支付失败: " + response.getSubMsg());
            }
            
        } catch (Exception e) {
            log.error("取消支付失败", e);
            return ApiResponse.fail(500, "取消支付失败: " + e.getMessage());
        }
    }
}

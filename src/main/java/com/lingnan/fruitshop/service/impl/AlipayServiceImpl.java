package com.lingnan.fruitshop.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.config.AlipayConfig;
import com.lingnan.fruitshop.service.AlipayService;
import com.lingnan.fruitshop.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付服务实现类
 */
@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {
    
    @Autowired
    private AlipayConfig alipayConfig;
    
    @Autowired
    private OrderService orderService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取支付宝客户端
     */
    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
            alipayConfig.getGatewayUrl(),
            alipayConfig.getAppId(),
            alipayConfig.getPrivateKey(),
            alipayConfig.getFormat(),
            alipayConfig.getCharset(),
            alipayConfig.getPublicKey(),
            alipayConfig.getSignType()
        );
    }
    
    /**
     * 创建网页支付订单
     */
    @Override
    public String createPagePay(Long orderId, String amount, String subject, String body) {
        try {
            // 1. 创建支付宝客户端
            AlipayClient alipayClient = getAlipayClient();
            
            // 2. 创建API请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            
            // 3. 设置回调地址
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setReturnUrl(alipayConfig.getReturnUrl());
            
            // 4. 设置业务参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderId.toString());           // 商户订单号
            bizContent.put("total_amount", amount);                        // 订单总金额
            bizContent.put("subject", subject);                            // 订单标题
            bizContent.put("body", body);                                  // 订单描述
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");     // 销售产品码
            bizContent.put("timeout_express", "30m");                     // 超时时间
            
            request.setBizContent(objectMapper.writeValueAsString(bizContent));
            
            // 5. 执行请求
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            
            if (response.isSuccess()) {
                // 6. 返回支付表单HTML
                return response.getBody();
            } else {
                log.error("支付宝创建支付订单失败: {}", response.getSubMsg());
                throw new RuntimeException("创建支付订单失败: " + response.getSubMsg());
            }
            
        } catch (AlipayApiException | JsonProcessingException e) {
            log.error("支付宝API调用异常", e);
            throw new RuntimeException("支付服务异常", e);
        }
    }
    
    /**
     * 查询支付状态
     */
    @Override
    public AlipayTradeQueryResponse queryPayStatus(String orderId) {
        try {
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderId);
            request.setBizContent(objectMapper.writeValueAsString(bizContent));
            
            return alipayClient.certificateExecute(request);
            
        } catch (AlipayApiException | JsonProcessingException e) {
            log.error("查询支付状态异常", e);
            throw new RuntimeException("查询支付状态失败", e);
        }
    }
    
    /**
     * 关闭支付订单
     */
    @Override
    public AlipayTradeCloseResponse closePay(String orderId) {
        try {
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderId);
            request.setBizContent(objectMapper.writeValueAsString(bizContent));
            
            return alipayClient.certificateExecute(request);
            
        } catch (AlipayApiException | JsonProcessingException e) {
            log.error("关闭支付订单异常", e);
            throw new RuntimeException("关闭支付订单失败", e);
        }
    }
    
    /**
     * 验证支付宝异步通知
     */
    @Override
    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
            );
        } catch (AlipayApiException e) {
            log.error("验证支付宝通知签名失败", e);
            return false;
        }
    }
    
    /**
     * 处理支付宝异步通知
     */
    @Override
    public String handleNotify(Map<String, String> params) {
        try {
            // 1. 验证签名
            if (!verifyNotify(params)) {
                log.error("支付宝通知签名验证失败");
                return "failure";
            }
            
            // 2. 获取关键参数
            String orderId = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            BigDecimal totalAmount = new BigDecimal(params.get("total_amount"));
            
            log.info("收到支付宝异步通知: orderId={}, tradeNo={}, tradeStatus={}, amount={}", 
                orderId, tradeNo, tradeStatus, totalAmount);
            
            // 3. 处理支付成功
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 更新订单状态
                boolean success = orderService.handlePaymentSuccess(
                    Long.parseLong(orderId), 
                    tradeNo, 
                    totalAmount,
                    "alipay"
                );
                
                if (success) {
                    log.info("订单支付处理成功: {}", orderId);
                    return "success";
                } else {
                    log.error("订单支付处理失败: {}", orderId);
                    return "failure";
                }
            }
            
            // 4. 其他状态暂时不处理
            log.info("支付宝通知状态暂不处理: {}", tradeStatus);
            return "success";
            
        } catch (Exception e) {
            log.error("处理支付宝通知异常", e);
            return "failure";
        }
    }
}

package com.lingnan.fruitshop.service;

import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;

/**
 * 支付宝支付服务接口
 */
public interface AlipayService {
    
    /**
     * 创建网页支付订单
     * @param orderId 订单ID
     * @param amount 支付金额
     * @param subject 商品名称
     * @param body 商品描述
     * @return 支付表单HTML
     */
    String createPagePay(Long orderId, String amount, String subject, String body);
    
    /**
     * 查询支付状态
     * @param orderId 商户订单号
     * @return 支付状态
     */
    AlipayTradeQueryResponse queryPayStatus(String orderId);
    
    /**
     * 关闭支付订单
     * @param orderId 商户订单号
     * @return 关闭结果
     */
    AlipayTradeCloseResponse closePay(String orderId);
    
    /**
     * 验证支付宝异步通知
     * @param params 支付宝回调参数
     * @return 验证结果
     */
    boolean verifyNotify(java.util.Map<String, String> params);
    
    /**
     * 处理支付宝异步通知
     * @param params 支付宝回调参数
     * @return 处理结果
     */
    String handleNotify(java.util.Map<String, String> params);
}

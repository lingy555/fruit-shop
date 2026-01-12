package com.lingnan.fruitshop.test;

import com.lingnan.fruitshop.config.AlipayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 支付宝回调地址测试
 */
@SpringBootTest
public class AlipayCallbackTest {

    @Autowired
    private AlipayConfig alipayConfig;

    @Test
    public void testCallbackUrls() {
        System.out.println("=== 支付宝回调地址测试 ===");
        System.out.println("Return URL: " + alipayConfig.getReturnUrl());
        System.out.println("Notify URL: " + alipayConfig.getNotifyUrl());
        
        // 验证回调地址格式
        String returnUrl = alipayConfig.getReturnUrl();
        if (returnUrl != null && returnUrl.contains("localhost")) {
            System.out.println("✅ 回调地址包含localhost");
            
            if (returnUrl.contains(":3000")) {
                System.out.println("✅ 回调地址使用端口3000");
            } else if (returnUrl.contains(":5173")) {
                System.out.println("⚠️ 回调地址使用端口5173，可能需要修改");
            } else {
                System.out.println("❓ 回调地址使用其他端口");
            }
        } else {
            System.out.println("❌ 回调地址格式不正确");
        }
        
        // 验证路径
        if (returnUrl != null && returnUrl.endsWith("/payment/return")) {
            System.out.println("✅ 回调路径正确: /payment/return");
        } else {
            System.out.println("❌ 回调路径不正确");
        }
    }
}

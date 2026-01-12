package com.lingnan.fruitshop.test;

import com.lingnan.fruitshop.config.AlipayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 支付宝配置测试
 */
@SpringBootTest
public class AlipayConfigTest {

    @Autowired
    private AlipayConfig alipayConfig;

    @Test
    public void testAlipayConfig() {
        System.out.println("=== 支付宝配置测试 ===");
        System.out.println("App ID: " + alipayConfig.getAppId());
        System.out.println("Private Key: " + (alipayConfig.getPrivateKey() != null ? "已配置" : "未配置"));
        System.out.println("Public Key: " + (alipayConfig.getPublicKey() != null ? "已配置" : "未配置"));
        System.out.println("Gateway URL: " + alipayConfig.getGatewayUrl());
        System.out.println("Sign Type: " + alipayConfig.getSignType());
        System.out.println("Charset: " + alipayConfig.getCharset());
        System.out.println("Format: " + alipayConfig.getFormat());
        
        // 检查关键配置
        if (alipayConfig.getAppId() == null || alipayConfig.getAppId().isEmpty()) {
            System.out.println("❌ App ID 未配置");
        } else {
            System.out.println("✅ App ID 已配置");
        }
        
        if (alipayConfig.getPrivateKey() == null || alipayConfig.getPrivateKey().isEmpty()) {
            System.out.println("❌ Private Key 未配置");
        } else {
            System.out.println("✅ Private Key 已配置");
        }
        
        if (alipayConfig.getPublicKey() == null || alipayConfig.getPublicKey().isEmpty()) {
            System.out.println("❌ Public Key 未配置");
        } else {
            System.out.println("✅ Public Key 已配置");
        }
    }
}

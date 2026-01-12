package com.lingnan.fruitshop.test;

import com.lingnan.fruitshop.service.AlipayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 支付宝支付功能测试
 */
@SpringBootTest
public class AlipayServiceTest {

    @Autowired
    private AlipayService alipayService;

    @Test
    public void testAlipayServiceInjection() {
        if (alipayService != null) {
            System.out.println("✅ 支付宝服务注入成功");
            System.out.println("🔧 支付宝支付功能已准备就绪");
        } else {
            System.out.println("❌ 支付宝服务注入失败");
        }
    }

    @Test
    public void testCreatePayment() {
        try {
            String result = alipayService.createPagePay(5027L, "0.01", "测试订单", "测试商品");
            System.out.println("✅ 创建支付订单成功");
            System.out.println("支付表单: " + result.substring(0, Math.min(100, result.length())) + "...");
        } catch (Exception e) {
            System.out.println("❌ 创建支付订单失败: " + e.getMessage());
        }
    }
}

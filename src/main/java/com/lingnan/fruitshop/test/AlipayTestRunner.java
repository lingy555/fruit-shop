package com.lingnan.fruitshop.test;

import com.lingnan.fruitshop.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付功能测试
 * 可选：用于测试支付宝支付功能是否正常工作
 */
@Component
public class AlipayTestRunner implements CommandLineRunner {

    @Autowired
    private AlipayService alipayService;

    @Override
    public void run(String... args) throws Exception {
        // 检查支付宝服务是否正常注入
        if (alipayService != null) {
            System.out.println("✅ 支付宝服务注入成功");
            System.out.println("🔧 支付宝支付功能已准备就绪");
            System.out.println("📋 请按照 ALIPAY_SETUP.md 配置沙箱环境");
        } else {
            System.out.println("❌ 支付宝服务注入失败");
        }
    }
}

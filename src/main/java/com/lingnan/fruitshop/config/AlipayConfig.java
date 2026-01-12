package com.lingnan.fruitshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置类
 * 用于配置支付宝沙箱环境的参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    
    /**
     * 应用ID，沙箱环境需要使用沙箱应用ID
     */
    private String appId;
    
    /**
     * 商户私钥，用于签名
     */
    private String privateKey;
    
    /**
     * 支付宝公钥，用于验签
     */
    private String publicKey;
    
    /**
     * 支付宝网关地址，沙箱环境：https://openapi.alipaydev.com/gateway.do
     */
    private String gatewayUrl;
    
    /**
     * 签名算法，默认RSA2
     */
    private String signType = "RSA2";
    
    /**
     * 字符编码，默认UTF-8
     */
    private String charset = "UTF-8";
    
    /**
     * 格式，默认JSON
     */
    private String format = "json";
    
    /**
     * 支付宝公钥证书路径（可选）
     */
    private String publicCertPath;
    
    /**
     * 应用私钥证书路径（可选）
     */
    private String privateCertPath;
    
    /**
     * 是否使用证书模式
     */
    private boolean useCert = false;
    
    /**
     * 异步通知地址
     */
    private String notifyUrl;
    
    /**
     * 同步回调地址
     */
    private String returnUrl;
}

package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long shopId;

    private Long merchantId;

    private Integer status;

    private BigDecimal totalAmount;

    private BigDecimal shippingFee;

    private BigDecimal couponDiscount;

    private BigDecimal pointsDiscount;

    private BigDecimal actualAmount;

    private String paymentMethod;

    private String payMethod;        // 支付方式（alipay, wechat, balance）

    private String tradeNo;          // 第三方交易号

    private String remark;

    private String merchantRemark;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    private String addressDetail;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime deliverTime;

    private LocalDateTime receiveTime;

    private LocalDateTime finishTime;

    private LocalDateTime cancelTime;
}

package com.Market.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    // 订单ID
    private Long id;

    // 订单编号
    private String orderNo;

    // 商品ID
    private Long productId;

    // 商品标题
    private String productTitle;

    // 商品图片
    private String productImage;

    // 买家ID
    private Long buyerId;

    // 卖家ID
    private Long sellerId;

    // 订单总金额
    private BigDecimal totalPrice;

    // 订单状态
    private Integer status;

    // 订单状态文本
    private String statusText;

    // 物流单号
    private String logisticsNo;

    // 订单过期时间
    private LocalDateTime expireTime;

    // 创建时间
    private LocalDateTime createTime;
}

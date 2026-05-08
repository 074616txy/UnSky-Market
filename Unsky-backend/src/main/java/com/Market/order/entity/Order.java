package com.Market.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order{

    //订单ID
    private Long id;

    //订单编号
    private String orderNo;

    //买家ID
    private Long buyerId;

    //卖家ID
    private Long sellerId;

    //商品ID
    private Long productId;

    //订单总金额
    private BigDecimal totalPrice;

    //订单状态：0待付款，1待发货，2待收货，3已完成，-1已取消
    private Integer status;

    //物流单号
    private String logisticsNo;

    //订单过期时间
    private LocalDateTime expireTime;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;
}
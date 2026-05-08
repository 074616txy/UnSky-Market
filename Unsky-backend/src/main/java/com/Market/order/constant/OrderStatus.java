package com.Market.order.constant;

/**
 * 订单状态常量
 */
public class OrderStatus {

    //待付款
    public static final Integer WAIT_PAY = 0;

    //待发货
    public static final Integer WAIT_SHIP = 1;

    //待收货
    public static final Integer WAIT_RECEIVE = 2;

    //已完成
    public static final Integer FINISHED = 3;

    //已取消
    public static final Integer CANCELED = -1;
}
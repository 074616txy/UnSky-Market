package com.Market.order.service;

import com.Market.common.result.Result;
import com.Market.order.dto.CheckoutOrderDTO;
import com.Market.order.dto.CreateOrderDTO;
import com.Market.order.dto.ShipOrderDTO;
import com.Market.order.vo.OrderVO;

import java.util.List;

public interface OrderService {

    /**
     * 创建订单
     * @param createOrderDTO
     * @param buyerId
     * @return
     */
    Result<Void> createOrder(CreateOrderDTO createOrderDTO, Long buyerId);

    /**
     * 查询我的订单列表
     * @param userId
     * @param status 可选状态
     * @return
     */
    Result<List<OrderVO>> listMyOrders(Long userId , Integer status);

    /**
     * 取消订单
     * @param orderId
     * @param userId
     * @return
     */
    Result<Void> cancelOrder(Long orderId, Long userId);

    /**
     * 卖家发货
     * @param shipOrderDTO
     * @param sellerId
     * @return
     */
    Result<Void> shipOrder(ShipOrderDTO shipOrderDTO, Long sellerId);

    /**
     * 买家确认收货
     * @param orderId
     * @param buyerId
     * @return
     */
    Result<Void> confirmOrder(Long orderId, Long buyerId);

    /**
     * 购物车结算
     * @param checkoutOrderDTO
     * @param buyerId
     * @return
     */
    Result<Void> checkoutOrder(CheckoutOrderDTO checkoutOrderDTO, Long buyerId);



}

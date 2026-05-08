package com.Market.order.controller;

import com.Market.common.result.Result;
import com.Market.common.util.JwtUtil;
import com.Market.order.dto.CheckoutOrderDTO;
import com.Market.order.dto.CreateOrderDTO;
import com.Market.order.dto.ShipOrderDTO;
import com.Market.order.service.OrderService;
import com.Market.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 立即购买创建订单
     * @param createOrderDTO
     * @param request
     * @return
     */
    @PostMapping("/create")
    public Result<Void> createOrder(@RequestBody CreateOrderDTO createOrderDTO,
                                    HttpServletRequest request) {
        String token = request.getHeader("token");
        Long buyerId = JwtUtil.getUserIdFromToken(token);
        return orderService.createOrder(createOrderDTO, buyerId);
    }

    /**
     * 查询我的订单列表
     * @param status
     * @param request
     * @return
     */
    @GetMapping("/list")
    public Result<List<OrderVO>> listMyOrders(@RequestParam(required = false) Integer status,
                                              HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);
        return orderService.listMyOrders(userId, status);
    }

    /**
     * 取消订单
     * @param orderId
     * @param request
     * @return
     */
    @PutMapping("/cancel/{orderId}")
    public Result<Void> cancelOrder(@PathVariable Long orderId,
                                    HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);
        return orderService.cancelOrder(orderId, userId);
    }

    /**
     * 卖家发货
     * @param shipOrderDTO
     * @param request
     * @return
     */
    @PutMapping("/ship")
    public Result<Void> shipOrder(@RequestBody ShipOrderDTO shipOrderDTO,
                                  HttpServletRequest request) {
        String token = request.getHeader("token");
        Long sellerId = JwtUtil.getUserIdFromToken(token);
        return orderService.shipOrder(shipOrderDTO, sellerId);
    }

    /**
     * 买家确认收货
     * @param orderId
     * @param request
     * @return
     */
    @PutMapping("/confirm/{orderId}")
    public Result<Void> confirmOrder(@PathVariable Long orderId,
                                     HttpServletRequest request) {
        String token = request.getHeader("token");
        Long buyerId = JwtUtil.getUserIdFromToken(token);
        return orderService.confirmOrder(orderId, buyerId);
    }

    /**
     * 购物车结算
     * @param checkoutOrderDTO
     * @param request
     * @return
     */
    @PostMapping("/checkout")
    public Result<Void> checkoutOrder(@RequestBody CheckoutOrderDTO checkoutOrderDTO,
                                      HttpServletRequest request) {
        String token = request.getHeader("token");
        Long buyerId = JwtUtil.getUserIdFromToken(token);
        return orderService.checkoutOrder(checkoutOrderDTO, buyerId);
    }





}


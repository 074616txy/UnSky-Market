package com.Market.order.service.impl;

import com.Market.common.entity.Cart;
import com.Market.common.entity.Product;
import com.Market.common.result.Result;
import com.Market.order.constant.OrderStatus;
import com.Market.order.dto.CheckoutOrderDTO;
import com.Market.order.dto.CreateOrderDTO;
import com.Market.order.dto.ShipOrderDTO;
import com.Market.order.entity.Order;
import com.Market.order.mapper.OrderMapper;
import com.Market.order.service.OrderService;
import com.Market.order.vo.OrderVO;
import com.Market.product.mapper.CartMapper;
import com.Market.product.mapper.ProductMapper;
import com.Market.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductMapper productMapper;

    private final CartMapper cartMapper;

    private final OrderMapper orderMapper;

    /**
     * 创建订单
     * @param createOrderDTO
     * @param buyerId
     * @return
     */
    @Override
    public Result<Void> createOrder(CreateOrderDTO createOrderDTO, Long buyerId) {
        // 1. 判断商品ID是否为空
        if (createOrderDTO == null || createOrderDTO.getProductId() == null) {
            return Result.error("商品ID不能为空");
        }

        // 2. 查询商品是否存在
        Product product = productMapper.selectById(createOrderDTO.getProductId());
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 3. 判断商品是否上架
        if (product.getStatus() == null || product.getStatus() != 1) {
            return Result.error("商品当前不可购买");
        }

        // 4. 防止用户购买自己发布的商品
        if (product.getSellerId().equals(buyerId)) {
            return Result.error("不能购买自己发布的商品");
        }

        // 5. 创建订单对象
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setTotalPrice(product.getPrice());
        order.setStatus(OrderStatus.WAIT_PAY);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));

        // 6. 插入订单
        orderMapper.insert(order);

        return Result.success();
    }

    /**
     * 查询我的订单列表
     * @param userId
     * @param status
     * @return
     */
    @Override
    public Result<List<OrderVO>> listMyOrders(Long userId, Integer status) {
        // 1. 查询当前用户作为买家的订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, userId);

        // 2. 如果传了状态，就按状态筛选
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        List<Order> orderList = orderMapper.selectList(wrapper);

        // 3. 转换为 VO
        List<OrderVO> voList = orderList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }


    // 转换订单VO
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();

        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setProductId(order.getProductId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setTotalPrice(order.getTotalPrice());
        vo.setStatus(order.getStatus());
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setLogisticsNo(order.getLogisticsNo());
        vo.setExpireTime(order.getExpireTime());
        vo.setCreateTime(order.getCreateTime());

        // 查询商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductImage(product.getImages());
        }

        return vo;
    }

    // 转换订单状态文本
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知状态";
        }

        switch (status) {
            case 0:
                return "待付款";
            case 1:
                return "待发货";
            case 2:
                return "待收货";
            case 3:
                return "已完成";
            case -1:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    /**
     * 取消订单
     * @param orderId
     * @param userId
     * @return
     */
    @Override
    public Result<Void> cancelOrder(Long orderId, Long userId) {
        // 1. 判断订单ID是否为空
        if (orderId == null) {
            return Result.error("订单ID不能为空");
        }

        // 2. 查询订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 3. 判断是否是当前用户自己的订单
        if (!order.getBuyerId().equals(userId)) {
            return Result.error("只能取消自己的订单");
        }

        // 4. 只有待付款订单可以取消
        if (!order.getStatus().equals(OrderStatus.WAIT_PAY)) {
            return Result.error("当前订单状态不可取消");
        }

        // 5. 修改订单状态为已取消
        order.setStatus(OrderStatus.CANCELED);
        orderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 卖家发货
     * @param shipOrderDTO
     * @param sellerId
     * @return
     */
    @Override
    public Result<Void> shipOrder(ShipOrderDTO shipOrderDTO, Long sellerId) {
        // 1. 判断参数是否为空
        if (shipOrderDTO == null || shipOrderDTO.getOrderId() == null) {
            return Result.error("订单ID不能为空");
        }

        // 2. 判断物流单号是否为空
        if (shipOrderDTO.getLogisticsNo() == null || shipOrderDTO.getLogisticsNo().trim().isEmpty()) {
            return Result.error("物流单号不能为空");
        }

        // 3. 查询订单是否存在
        Order order = orderMapper.selectById(shipOrderDTO.getOrderId());
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 4. 判断是否是当前卖家的订单
        if (!order.getSellerId().equals(sellerId)) {
            return Result.error("只能发货自己的订单");
        }

        // 5. 只有待发货订单可以发货
        if (!order.getStatus().equals(OrderStatus.WAIT_SHIP)) {
            return Result.error("当前订单状态不可发货");
        }

        // 6. 填写物流单号并修改状态为待收货
        order.setLogisticsNo(shipOrderDTO.getLogisticsNo());
        order.setStatus(OrderStatus.WAIT_RECEIVE);
        orderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 买家确认收货
     * @param orderId
     * @param buyerId
     * @return
     */
    @Override
    public Result<Void> confirmOrder(Long orderId, Long buyerId) {
        // 1. 判断订单ID是否为空
        if (orderId == null) {
            return Result.error("订单ID不能为空");
        }

        // 2. 查询订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 3. 判断是否是当前买家的订单
        if (!order.getBuyerId().equals(buyerId)) {
            return Result.error("只能确认自己的订单");
        }

        // 4. 只有待收货订单可以确认收货
        if (!order.getStatus().equals(OrderStatus.WAIT_RECEIVE)) {
            return Result.error("当前订单状态不可确认收货");
        }

        // 5. 修改订单状态为已完成
        order.setStatus(OrderStatus.FINISHED);
        orderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 购物车结算
     * @param checkoutOrderDTO
     * @param buyerId
     * @return
     */
    @Override
    @Transactional
    public Result<Void> checkoutOrder(CheckoutOrderDTO checkoutOrderDTO, Long buyerId) {
        // 1. 判断购物车ID列表是否为空
        if (checkoutOrderDTO == null || checkoutOrderDTO.getCartIds() == null || checkoutOrderDTO.getCartIds().isEmpty()) {
            return Result.error("请选择要结算的商品");
        }

        // 2. 遍历购物车ID，逐个生成订单
        for (Long cartId : checkoutOrderDTO.getCartIds()) {
            Cart cart = cartMapper.selectById(cartId);

            // 3. 判断购物车记录是否存在
            if (cart == null) {
                return Result.error("购物车记录不存在");
            }

            // 4. 判断是否是当前用户自己的购物车记录
            if (!cart.getUserId().equals(buyerId)) {
                return Result.error("只能结算自己的购物车商品");
            }

            // 5. 查询商品
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) {
                return Result.error("商品不存在");
            }

            // 6. 判断商品是否上架
            if (product.getStatus() == null || product.getStatus() != 1) {
                return Result.error("商品当前不可购买");
            }

            // 7. 防止购买自己发布的商品
            if (product.getSellerId().equals(buyerId)) {
                return Result.error("不能购买自己发布的商品");
            }

            // 8. 创建订单
            Order order = new Order();
            order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
            order.setBuyerId(buyerId);
            order.setSellerId(product.getSellerId());
            order.setProductId(product.getId());
            order.setTotalPrice(product.getPrice());
            order.setStatus(OrderStatus.WAIT_PAY);
            order.setExpireTime(LocalDateTime.now().plusMinutes(30));

            orderMapper.insert(order);

            // 9. 结算成功后删除购物车记录
            cartMapper.deleteById(cartId);
        }

        return Result.success();
    }






}



package com.Market.product.service.impl;

import com.Market.common.entity.Cart;
import com.Market.common.entity.Product;
import com.Market.common.result.Result;
import com.Market.product.mapper.CartMapper;
import com.Market.product.mapper.ProductMapper;
import com.Market.product.service.CartService;
import com.Market.product.vo.CartVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    /**
     * 加入购物车
     * @param productId
     * @param userId
     * @return
     */
    @Override
    public Result<Void> addCart(Long productId, Long userId) {
        // 1. 判断商品ID是否为空
        if (productId == null) {
            return Result.error("商品ID不能为空");
        }

        // 2. 判断是否已经加入购物车
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);

        Long count = cartMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("请勿重复加入购物车");
        }

        // 3. 新增购物车记录
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);

        cartMapper.insert(cart);

        return Result.success();
    }

    /**
     * 取消购物车商品
     * @param productId
     * @param userId
     * @return
     */
    @Override
    public Result<Void> removeCart(Long productId, Long userId) {
        // 1. 判断商品ID是否为空
        if (productId == null) {
            return Result.error("商品ID不能为空");
        }

        // 2. 根据 userId + productId 删除购物车记录
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);

        int rows = cartMapper.delete(wrapper);

        // 3. 如果没有删除到数据，说明该商品不在购物车中
        if (rows == 0) {
            return Result.error("该商品不在购物车中");
        }

        return Result.success();
    }

    /**
     * 我的购物车列表
     * @param userId
     * @return
     */
    @Override
    public Result<List<CartVO>> listMyCart(Long userId) {
        // 1. 查询当前用户购物车记录
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime);

        List<Cart> cartList = cartMapper.selectList(wrapper);

        // 2. 购物车为空，直接返回空列表
        if (cartList == null || cartList.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 3. 遍历购物车记录，查询对应商品信息
        List<CartVO> voList = cartList.stream().map(cart -> {
            Product product = productMapper.selectById(cart.getProductId());

            CartVO vo = new CartVO();
            vo.setCartId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setCreateTime(cart.getCreateTime());

            if (product != null) {
                vo.setTitle(product.getTitle());
                vo.setPrice(product.getPrice());
                vo.setImages(product.getImages());
                vo.setViewCount(product.getViewCount());
            }

            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

}

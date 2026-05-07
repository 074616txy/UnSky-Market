package com.Market.product.service;

import com.Market.common.entity.Cart;
import com.Market.common.result.Result;
import com.Market.product.vo.CartVO;

import java.util.List;

public interface CartService {

    /**
     * 加入购物车
     * @param productId
     * @param userId
     * @return
     */
    Result<Void> addCart(Long productId, Long userId);

    /**
     * 删除购物车商品
     * @param productId
     * @param userId
     * @return
     */
    Result<Void> removeCart(Long productId, Long userId);

    /**
     * 我的购物车列表
     * @param userId
     * @return
     */
    Result<List<CartVO>> listMyCart(Long userId);

}

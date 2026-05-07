package com.Market.product.service;

import com.Market.common.result.Result;
import com.Market.product.vo.FavoriteVO;

import java.util.List;

public interface FavoriteService {

    /**
     * 收藏商品
     * @param productId
     * @param userId
     * @return
     */
    Result<Void> addFavorite(Long productId, Long userId);

    /**
     * 取消收藏
     * @param productId
     * @param userId
     * @return
     */
    Result<Void> cancelFavorite(Long productId, Long userId);

    /**
     * 我的收藏列表
     * @param userId
     * @return
     */
    Result<List<FavoriteVO>> listMyFavorites(Long userId);

}

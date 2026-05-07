package com.Market.product.service.impl;

import com.Market.common.entity.Favorite;
import com.Market.common.entity.Product;
import com.Market.common.result.Result;
import com.Market.product.mapper.FavoriteMapper;
import com.Market.product.mapper.ProductMapper;
import com.Market.product.service.FavoriteService;
import com.Market.product.vo.FavoriteVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    /**
     * 添加收藏
     * @param productId
     * @param userId
     * @return
     */
    @Override
    public Result<Void> addFavorite(Long productId, Long userId) {
        // 1. 判断商品ID是否为空
        if (productId == null) {
            return Result.error("商品ID不能为空");
        }
        // 2. 判断是否已经收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);

        Long count = favoriteMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("请勿重复收藏");
        }

        // 3. 新增收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);

        favoriteMapper.insert(favorite);

        return Result.success();
    }

    /**
     * 取消收藏
     * @param productId
     * @param userId
     * @return
     */
    @Override
    public Result<Void> cancelFavorite(Long productId, Long userId) {
        // 1. 判断商品ID是否为空
        if (productId == null) {
            return Result.error("商品ID不能为空");
        }

        // 2. 根据 userId + productId 删除收藏记录
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);

        int rows = favoriteMapper.delete(wrapper);

        // 3. 如果没有删除到数据，说明原本没有收藏
        if (rows == 0) {
            return Result.error("当前商品未收藏");
        }

        return Result.success();
    }

    /**
     * 我的收藏列表
     * @param userId
     * @return
     */
    @Override
    public Result<List<FavoriteVO>> listMyFavorites(Long userId) {
        // 1. 查询当前用户的收藏记录
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);

        List<Favorite> favoriteList = favoriteMapper.selectList(wrapper);

        // 2. 收藏记录为空，直接返回空列表
        if (favoriteList == null || favoriteList.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 3. 遍历收藏记录，查询对应商品信息
        List<FavoriteVO> voList = favoriteList.stream().map(favorite -> {
            Product product = productMapper.selectById(favorite.getProductId());

            FavoriteVO vo = new FavoriteVO();
            vo.setFavoriteId(favorite.getId());
            vo.setProductId(favorite.getProductId());
            vo.setCreateTime(favorite.getCreateTime());

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

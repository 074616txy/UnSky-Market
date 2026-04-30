package com.Market.product.service;

import com.Market.common.entity.Product;
import com.Market.common.result.Result;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品业务接口
 */
public interface ProductService {
     /**
     * 查询商品列表----大概信息
     * 当前阶段先查询所有上架商品，
     * 后续再逐步扩展分类筛选、关键词搜索、价格区间、分页等功能。
     * @return 商品列表
     */
     Result<List<Product>> listProduct();

    /**
     * 根据商品id查询详情信息
     * @param id 提供具体商品id
     * @return 商品详情信息
     */
    Result<Product> getProductDetail(Long id);

    /**
     * 按分类筛选商品列表  (已经被关键词搜索合并用法)
     * @param categoryId 分类ID（可选，传null表示查全部）
     * @return 该分类下的上架商品列表
     */
    Result<List<Product>> listProductByCategory(Long categoryId);

    /**
     * 商品列表查询（支持分类筛选 + 关键词搜索 + 价格区间筛选）
     * @param categoryId 分类ID（可选，传null表示不限制）
     * @param keyword 搜索关键词（可选，传null表示查全部）
     * @param minPrice 最低价格（可选，传null表示不限制最低价）
     * @param maxPrice 最高价格（可选，传null表示不限制最高价）
     * @return 匹配的上架商品列表
     */
    Result<List<Product>> searchProducts(Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 发布商品
     * @param product 商品信息（包含标题、价格、分类等）
     * @param userId 卖家ID（从Token中解析出来）
     * @return 发布结果
     */
    Result<Void> publishProduct(Product product, Long userId);

    /**
     * 编辑商品
     * @param product 商品信息（包含要修改的字段）
     * @param userId 当前登录用户ID（用于校验是否是卖家）
     * @return 编辑结果
     */
    Result<Void> updateProduct(Product product, Long userId);

    /**
     * 删除商品
     * @param id 要删除商品的ID--删什么
     * @param userId 当前登录用户ID（用于校验是否是卖家）--谁在删
     * @return 删除结果
     */
    Result<Void> deleteProduct(Long id, Long userId);

    /**
     * 查询我发布的商品列表
     * @param userId 当前登录用户ID
     * @return 我发布的商品列表
     */
    Result<List<Product>> getMyProducts(Long userId);
}


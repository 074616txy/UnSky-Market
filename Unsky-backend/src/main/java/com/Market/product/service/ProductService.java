package com.Market.product.service;

import com.Market.common.entity.Product;
import com.Market.common.result.Result;
import com.Market.product.dto.ProductPublishDTO;
import com.Market.product.dto.ProductUpdateDTO;
import com.Market.product.query.ProductQuery;
import com.Market.product.vo.ProductDetailVO;
import com.Market.product.vo.ProductListVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

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
    Result<ProductDetailVO> getProductDetail(Long id);

    /**
     * 按分类筛选商品列表  (已经被关键词搜索合并用法)
     * @param categoryId 分类ID（可选，传null表示查全部）
     * @return 该分类下的上架商品列表
     */
    Result<List<Product>> listProductByCategory(Long categoryId);

    /**
     * 商品列表查询（支持分类筛选 + 关键词搜索 + 价格区间筛选）
     * 修改返回值类型为分页参数 <IPage<Product>>----day06🌿2.1
     * 修改返回值类型为VO----day06🌿3.1
     * @param productQuery 封装查询参数
     * @return 匹配的上架商品列表
     */
    Result<IPage<ProductListVO>> searchProducts(ProductQuery productQuery);

    /**
     * 发布商品
     * @param productPublishDTO 商品信息（包含标题、价格、分类等）
     * @param userId 卖家ID（从Token中解析出来）
     * @return 发布结果
     */
    Result<Void> publishProduct(ProductPublishDTO productPublishDTO, Long userId);

    /**
     * 编辑商品
     * @param productUpdateDTO 商品信息（包含要修改的字段）
     * @param userId 当前登录用户ID（用于校验是否是卖家）
     * @return 编辑结果
     */
    Result<Void> updateProduct(ProductUpdateDTO productUpdateDTO, Long userId);

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

    /**
     * 热门商品展示
     * @param limit
     * @return
     */
    Result<List<ProductListVO>> listHotProducts(Integer limit);
}


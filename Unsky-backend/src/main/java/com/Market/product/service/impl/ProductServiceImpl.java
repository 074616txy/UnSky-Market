package com.Market.product.service.impl;

import com.Market.common.entity.Product;
import com.Market.common.result.Result;
import com.Market.product.mapper.ProductMapper;
import com.Market.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品业务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    /**
     * 查询商品列表
     * 当前阶段只查询上架商品，并按照创建时间倒序返回。
     *
     * @return 商品列表
     */
    @Override
    public Result<List<Product>> listProduct() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime);

        List<Product> productList = productMapper.selectList(wrapper);

        return Result.success(productList);
    }

    /**
     * 根据商品id查询具体商品详情信息
     *
     * @param id 提供具体商品id
     * @return
     */
    @Override
    public Result<Product> getProductDetail(Long id) {
        // 1. 先查询商品（只查上架的）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, id)
                .eq(Product::getStatus, 1);  // 只查上架的商品

        Product product = productMapper.selectOne(wrapper);

        // 2. 判断商品是否存在
        if (product == null) {
            return Result.error("商品不存在或已下架");
        }
        //后续会扩展浏览量业务
        return Result.success(product);
    }

    /**
     * 按分类筛选商品列表  (已经被关键词搜索合并用法)
     *
     * @param categoryId 分类ID（可选，传null表示查全部）
     * @return 该分类下的上架商品列表
     */
    @Override
    public Result<List<Product>> listProductByCategory(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 1. 只传上架商品
        wrapper.eq(Product::getStatus, 1);

        // 2. 如果传了categoryId，就加分类筛选条件
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 3. 按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);

        List<Product> productList = productMapper.selectList(wrapper);
        return Result.success(productList);
    }

    /**
     * 商品列表查询（支持分类筛选 + 关键词搜索 + 价格区间筛选）
     *
     * @param categoryId 分类ID（可选，传null表示不限制）
     * @param keyword    搜索关键词（可选，传null表示查全部）
     * @param minPrice   最低价格（可选，传null表示不限制最低价）
     * @param maxPrice   最高价格（可选，传null表示不限制最高价）
     * @return 匹配的上架商品列表
     */
    @Override
    public Result<List<Product>> searchProducts(Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 1. 只查上架商品
        wrapper.eq(Product::getStatus, 1);

        // 2. 如果传了categoryId，就加分类筛选条件
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 3. 如果传了keyword，就做模糊匹配
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Product::getTitle, keyword)
                    .or()
                    .like(Product::getDescription, keyword)
            );
            //keyword.trim().isEmpty() 多加一个判断：用户可能传了空格，空格不应该被当作有效关键词
            //使用 like 而不是 eq：like 是模糊匹配，eq 是精确匹配，搜索场景必须用 like
        }

        // 4. 如果传了最低价格，就筛选 price >= minPrice
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }

        // 5. 如果传了最高价格，就筛选 price <= maxPrice
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }

        // 4. 按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);

        List<Product> productList = productMapper.selectList(wrapper);
        return Result.success(productList);
    }

    /**
     * 发布商品
     *
     * @param product 商品信息
     * @param userId  卖家ID
     * @return 发布结果
     */
    @Override
    public Result<Void> publishProduct(Product product, Long userId) {
        // 1. 基础校验
        if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
            return Result.error("商品标题不能为空");
        }
        if (product.getPrice() == null) {
            return Result.error("商品价格不能为空");
        }
        if (product.getCategoryId() == null) {
            return Result.error("请选择商品分类");
        }

        // 2. 设置默认值
        product.setSellerId(userId);           // 卖家ID = 当前登录用户
        product.setStatus((byte) 1);           // 上架状态（1=上架）
        product.setViewCount(0);               // 浏览量初始为0
        product.setFavoriteCount(0);            // 收藏量初始为0

        // 3. 写入数据库
        productMapper.insert(product);

        return Result.success();
    }

    /**
     * 编辑商品
     *
     * @param product 商品信息（包含商品ID和要修改的字段）
     * @param userId  当前登录用户ID
     * @return 编辑结果
     */
    @Override
    public Result<Void> updateProduct(Product product, Long userId) {
        // 1. 根据商品ID查询商品
        Product existProduct = productMapper.selectById(product.getId());

        // 2. 判断商品是否存在
        if (existProduct == null) {
            return Result.error("商品不存在");
        }

        // 3. 校验权限：只有卖家本人才能编辑
        if (!existProduct.getSellerId().equals(userId)) {
            return Result.error("无权操作他人的商品");
        }

        // 4. 基础校验
        if (product.getTitle() != null && product.getTitle().trim().isEmpty()) {
            return Result.error("商品标题不能为空");
        }
        if (product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return Result.error("商品价格不能为负数");
        }

        // 5. 更新字段（只更新传了值的字段）
        if (product.getTitle() != null) {
            existProduct.setTitle(product.getTitle());
        }
        if (product.getDescription() != null) {
            existProduct.setDescription(product.getDescription());
        }
        if (product.getPrice() != null) {
            existProduct.setPrice(product.getPrice());
        }
        if (product.getCategoryId() != null) {
            existProduct.setCategoryId(product.getCategoryId());
        }
        if (product.getConditionLevel() != null) {
            existProduct.setConditionLevel(product.getConditionLevel());
        }

        // 6. 写入数据库
        productMapper.updateById(existProduct);

        return Result.success();
    }

    /**
     * 删除商品
     *
     * @param id     商品ID
     * @param userId 当前登录用户ID
     * @return 删除结果
     */
    @Override
    public Result<Void> deleteProduct(Long id, Long userId) {
        // 1. 根据ID查询商品
        Product existProduct = productMapper.selectById(id);

        // 2. 判断商品是否存在
        if (existProduct == null) {
            return Result.error("商品不存在");
        }

        // 3. 校验权限：只有卖家本人才能删除
        if (!existProduct.getSellerId().equals(userId)) {
            return Result.error("无权操作他人的商品");
        }

        // 4. 执行删除
        productMapper.deleteById(id);

        return Result.success();
    }

    /**
     * 查询我发布的商品列表
     * @param userId 当前登录用户ID
     * @return 我发布的商品列表
     */
    @Override
    public Result<List<Product>> getMyProducts(Long userId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 根据sellerId查询当前用户发布的商品
        wrapper.eq(Product::getSellerId, userId)
                .orderByDesc(Product::getCreateTime);  // 按发布时间倒序

        List<Product> productList = productMapper.selectList(wrapper);

        return Result.success(productList);
    }
}
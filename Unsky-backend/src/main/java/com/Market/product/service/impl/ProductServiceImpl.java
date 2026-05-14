package com.Market.product.service.impl;

import com.Market.common.entity.Product;
import com.Market.common.result.Result;
import com.Market.product.dto.ProductPublishDTO;
import com.Market.product.dto.ProductUpdateDTO;
import com.Market.product.mapper.ProductMapper;
import com.Market.product.query.ProductQuery;
import com.Market.product.service.ProductService;
import com.Market.product.vo.ProductDetailVO;
import com.Market.product.vo.ProductListVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
     * 商品详情接口 - 浏览量统计（Redis缓存）
     * @param id 提供具体商品id
     * @return
     */
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result<ProductDetailVO> getProductDetail(Long id) {
        // 1. 先查询商品（只查上架的）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, id)
                .eq(Product::getStatus, 1);  // 只查上架的商品

        Product product = productMapper.selectOne(wrapper);

        // 2. 判断商品是否存在
        if (product == null) {
            return Result.error("商品不存在或已下架");
        }

        // Redis Key。部署学习阶段如果 Redis 未启动，就降级为直接更新数据库浏览量。
        String key = "product:viewCount:" + id;
        try {
            Long viewCount = redisTemplate.opsForValue().increment(key, 1);
            product.setViewCount(viewCount.intValue());
        } catch (Exception e) {
            System.out.println("Redis 不可用，浏览量降级为数据库自增：" + e.getMessage());
            product.setViewCount(product.getViewCount() + 1);
        }
        productMapper.updateById(product);
        //后续会扩展浏览量业务
        return Result.success(convertToDetailVO(product));
    }

    /**
     * 按分类筛选商品列表  (已经被关键词搜索合并用法)
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
     * @param productQuery 封装查询参数
     * @return 匹配的上架商品列表
     */
    @Override
    public Result<IPage<ProductListVO>> searchProducts(ProductQuery productQuery) {
        // 1. 创建分页对象
        Page<Product> page = new Page<>(
                productQuery.getPageNum(),
                productQuery.getPageSize()
        );
        // 2. 创建查询条件构造器
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 3. 固定条件：只查上架商品
        wrapper.eq(Product::getStatus, 1);

        // 4. 动态条件：如果传了categoryId，就加分类筛选条件
        if (productQuery.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, productQuery.getCategoryId());
        }

        // 5. 动态条件：如果传了keyword，就做模糊匹配----关键词搜索
        if (StringUtils.hasText(productQuery.getKeyword())) {
            wrapper.and(w -> w
                    .like(Product::getTitle, productQuery.getKeyword())
                    .or()
                    .like(Product::getDescription, productQuery.getKeyword())
            );
            //productQuery.getKeyword().trim().isEmpty() 多加一个判断：用户可能传了空格，空格不应该被当作有效关键词
            //使用 like 而不是 eq：like 是模糊匹配，eq 是精确匹配，搜索场景必须用 like
        }

        // 6. 动态条件：如果传了最低价格，就筛选 price >= minPrice
        if (productQuery.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, productQuery.getMinPrice());
        }

        // 7. 动态条件：如果传了最高价格，就筛选 price <= maxPrice
        if (productQuery.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, productQuery.getMaxPrice());
        }

        // 7.5 按创建时间倒序；若时间相同，则按ID倒序，保证排序稳定性（分页场景必须）
        //这是2.1的版本，2.2将它合并为多条件排序
        //wrapper.orderByDesc(Product::getCreateTime);
        //wrapper.orderByDesc(Product::getId);

        // 8. 动态排序：可以个根据价格，浏览量，发布时间来具体排序
        if (productQuery.getSortBy() != null&& !productQuery.getSortBy().isEmpty()) {
            switch (productQuery.getSortBy()) {
                case "price_asc":
                    wrapper.orderByAsc(Product::getPrice);
                    break;
                case "price_desc":
                    wrapper.orderByDesc(Product::getPrice);
                    break;
                case "view_count":
                    wrapper.orderByDesc(Product::getViewCount);
                    break;
                case "create_time_desc":
                    wrapper.orderByDesc(Product::getCreateTime);
                    break;
                default:
                    wrapper.orderByDesc(Product::getCreateTime);
                    break;
            }
        } else {
            // 9. 默认排序：最新发布优先（来源于2.1）
            wrapper.orderByDesc(Product::getCreateTime);
        }
        // 10. 稳定排序：避免分页时相同时间数据顺序不稳定
        //全局兜底，保证在任意排序条件下，当字段值相同时，结果顺序稳定，直接按照id倒序排序
        wrapper.orderByDesc(Product::getId);

        // 11. 执行分页查询，查询结果中的 records 仍然是 Product 实体对象
        IPage<Product> resultPage = productMapper.selectPage(page, wrapper);
        //List<Product> productList = productMapper.selectList(wrapper);分页查询替换查询列表

        // 12. 将当前页的 Product 列表转换为 ProductListVO 列表
        List<ProductListVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 13. 创建一个新的 VO 分页对象，用于返回给前端
        IPage<ProductListVO> voPage = new Page<>(
                resultPage.getCurrent(),
                resultPage.getSize()
        );
        // 14. 复制分页总条数
        voPage.setTotal(resultPage.getTotal());
        // 15. 设置转换后的 VO 列表
        voPage.setRecords(voList);

        // 16. 返回分页结果VO
        return Result.success(voPage);
    }

    /**
     * 发布商品
     * @param productPublishDTO 商品信息
     * @param userId  卖家ID
     * @return 发布结果
     */
    @Override
    public Result<Void> publishProduct(ProductPublishDTO productPublishDTO, Long userId) {
        // 1. 防重复提交：同一用户短时间内不能重复发布相同商品
        LambdaQueryWrapper<Product> repeatWrapper = new LambdaQueryWrapper<>();
        repeatWrapper.eq(Product::getSellerId, userId)
                .eq(Product::getTitle, productPublishDTO.getTitle().trim())
                .eq(Product::getPrice, productPublishDTO.getPrice())
                .ge(Product::getCreateTime, LocalDateTime.now().minusMinutes(1));

        Long count = productMapper.selectCount(repeatWrapper);
        if (count > 0) {
            return Result.error("请勿重复发布相同商品");
        }

        // 2. DTO 转 Entity
        Product product = new Product();

        product.setTitle(productPublishDTO.getTitle().trim());
        product.setDescription(productPublishDTO.getDescription());
        product.setPrice(productPublishDTO.getPrice());
        product.setOriginalPrice(productPublishDTO.getOriginalPrice());
        product.setCategoryId(productPublishDTO.getCategoryId());
        product.setConditionLevel(productPublishDTO.getConditionLevel());
        product.setImages(productPublishDTO.getImages());

        // 3. 设置系统字段
        product.setSellerId(userId);
        product.setStatus((byte) 1);
        product.setViewCount(0);
        product.setFavoriteCount(0);

        // 4. 写入数据库
        productMapper.insert(product);

        return Result.success();
    }

    /**
     * 编辑商品
     * @param productUpdateDTO 商品信息（包含商品ID和要修改的字段）
     * @param userId  当前登录用户ID
     * @return 编辑结果
     */
    @Override
    public Result<Void> updateProduct(ProductUpdateDTO productUpdateDTO, Long userId) {

        // 1. 查询
        Product existProduct = productMapper.selectById(productUpdateDTO.getId());
        if (existProduct == null) {
            return Result.error("商品不存在");
        }

        // 2. 权限校验
        if (!existProduct.getSellerId().equals(userId)) {
            return Result.error("无权操作他人的商品");
        }

        // 3. 更新字段（只更新非空字段）

        if (productUpdateDTO.getTitle() != null) {
            existProduct.setTitle(productUpdateDTO.getTitle().trim());
        }

        if (productUpdateDTO.getDescription() != null) {
            existProduct.setDescription(productUpdateDTO.getDescription());
        }

        if (productUpdateDTO.getPrice() != null) {
            existProduct.setPrice(productUpdateDTO.getPrice());
        }

        if (productUpdateDTO.getOriginalPrice() != null) {
            existProduct.setOriginalPrice(productUpdateDTO.getOriginalPrice());
        }

        if (productUpdateDTO.getCategoryId() != null) {
            existProduct.setCategoryId(productUpdateDTO.getCategoryId());
        }

        if (productUpdateDTO.getConditionLevel() != null) {
            existProduct.setConditionLevel(productUpdateDTO.getConditionLevel());
        }

        if (productUpdateDTO.getImages() != null) {
            existProduct.setImages(productUpdateDTO.getImages());
        }

        // 4. 更新数据库
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

    /**
     * Entity → VO 转换方法
     * 作用：
     * 1. 完成字段映射（数据库字段 → 前端展示字段）
     * 2. 处理数据格式转换（如 JSON → List）
     * 3. 提供异常兜底，保证系统稳定性
     * @param product
     * @return
     */
    private ProductListVO convertToVO(Product product) {
        ProductListVO vo = new ProductListVO();
        vo.setId(product.getId());
        vo.setSellerId(product.getSellerId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setCategoryId(product.getCategoryId());
        vo.setViewCount(product.getViewCount());
        vo.setFavoriteCount(product.getFavoriteCount());
        vo.setConditionLevel(product.getConditionLevel());
        vo.setStatus(product.getStatus());
        vo.setCreateTime(product.getCreateTime());

        // 将 images 的 JSON 字符串转换为 List
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            try {
                vo.setImages(JSON.parseArray(product.getImages(), String.class));
            } catch (Exception e) {
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }

        return vo;
    }

    /**
     * Entity → 详情VO 转换方法
     * 作用：
     * 1. 完成字段映射（数据库字段 → 前端详情展示字段）
     * 2. 返回完整商品信息（区别于列表VO的精简字段）
     * 3. 处理数据格式转换（如 JSON → List）
     * 4. 提供异常兜底，保证系统稳定性
     * @param product
     * @return
     */
    private ProductDetailVO convertToDetailVO(Product product) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setSellerId(product.getSellerId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setCategoryId(product.getCategoryId());
        vo.setViewCount(product.getViewCount());
        vo.setFavoriteCount(product.getFavoriteCount());
        vo.setConditionLevel(product.getConditionLevel());
        vo.setStatus(product.getStatus());
        vo.setCreateTime(product.getCreateTime());

        // images JSON 转 List
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            try {
                vo.setImages(JSON.parseArray(product.getImages(), String.class));
            } catch (Exception e) {
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }

        return vo;
    }

    /**
     * 热门商品展示
     * @param limit
     * @return
     */
    @Override
    public Result<List<ProductListVO>> listHotProducts(Integer limit) {
        // 1. 处理默认条数
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 20) {
            limit = 20;
        }


        // 2. 查询上架商品，按浏览量倒序
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .orderByDesc(Product::getViewCount)
                .orderByDesc(Product::getId)
                .last("LIMIT " + limit);

        List<Product> productList = productMapper.selectList(wrapper);

        // 3. Entity 转 VO
        List<ProductListVO> voList = productList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }



}

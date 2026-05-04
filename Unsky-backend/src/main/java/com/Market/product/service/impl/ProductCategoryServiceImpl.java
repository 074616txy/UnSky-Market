package com.Market.product.service.impl;

import com.Market.common.entity.ProductCategory;
import com.Market.common.result.Result;
import com.Market.product.mapper.ProductCategoryMapper;
import com.Market.product.service.ProductCategoryService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor//自动生成“必需参数”的构造方法
/**
  主要用途：
  配合 private final
  省掉手写构造方法
  实现构造器注入
 */
public class ProductCategoryServiceImpl implements ProductCategoryService {
    // “必需参数” ----> final 修饰的字段
    private final ProductCategoryMapper productCategoryMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 查询分类列表
     */
    @Override
    public Result<List<ProductCategory>> listCategory() {
        // 1. 定义 Redis 缓存 Key
        String key = "product:category:list";

        // 2. 先查询 Redis
        String cacheJson = stringRedisTemplate.opsForValue().get(key);
        if (cacheJson != null && !cacheJson.isEmpty()) {
            List<ProductCategory> categoryList = JSON.parseArray(cacheJson, ProductCategory.class);
            return Result.success(categoryList);
        }

        // 3. Redis 未命中，查询数据库
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getStatus, 1) // 只返回启用的分类
                .orderByAsc(ProductCategory::getSort); // 按照 sort 进行固定排序

        List<ProductCategory> categoryList = productCategoryMapper.selectList(wrapper);

        // 4. 将数据库查询结果写入 Redis，设置 1 小时过期
        stringRedisTemplate.opsForValue().set(
                key,
                JSON.toJSONString(categoryList),
                1,
                TimeUnit.HOURS
        );
        System.out.println("分类缓存写入 Redis，key = " + key);

        // 5. 返回结果
        return Result.success(categoryList);
    }
}

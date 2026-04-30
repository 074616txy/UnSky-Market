package com.Market.product.service.impl;

import com.Market.common.entity.ProductCategory;
import com.Market.common.result.Result;
import com.Market.product.mapper.ProductCategoryMapper;
import com.Market.product.service.ProductCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor//自动生成“必需参数”的构造方法
/**
  主要用途：
  配合 private final
  省掉手写构造方法
  实现构造器注入
 */
public class ProductCategoryServiceImpl implements ProductCategoryService {
    //“必需参数”----> final 修饰的字段
    private final ProductCategoryMapper productCategoryMapper;

    /**
     * 查询分类列表
     */
    @Override
    public Result<List<ProductCategory>> listCategory() {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getStatus, 1)//只返回启用的列表
                .orderByAsc(ProductCategory::getSort);//按照sort来进行固定排序

        List<ProductCategory> categoryList = productCategoryMapper.selectList(wrapper);
        return Result.success(categoryList);
    }


}

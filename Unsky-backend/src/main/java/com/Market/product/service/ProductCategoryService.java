package com.Market.product.service;

import com.Market.common.entity.ProductCategory;
import com.Market.common.result.Result;

import java.util.List;

public interface ProductCategoryService {

    /**
     * 查询分类列表
     * 因为返回的是这一分类的所有数据，所以用List<ProductCategory>
     * 前阶段分类列表只负责返回所有启用分类，因此方法不需要额外参数
     * @return
     */
    Result<List<ProductCategory>> listCategory();
}

package com.Market.product.controller;

import com.Market.common.entity.ProductCategory;
import com.Market.common.result.Result;
import com.Market.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/list")//本质上是查询操作
    public Result<List<ProductCategory>> listCategory() {
        return productCategoryService.listCategory();
    }
}
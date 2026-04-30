package com.Market.product.controller;

import com.Market.common.entity.Product;
import com.Market.common.result.Result;
import com.Market.common.util.JwtUtil;
import com.Market.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 查询商品列表
     * @return
     *注意：这里把原来的 /list 接口替换掉了，因为新逻辑完全兼容旧逻辑，不需要两个接口。
    @GetMapping("/list")
    public Result<List<Product>> listProduct()
    {
        return productService.listProduct();
    }
     */

    /**
     * 根据商品id查询具体商品详情信息
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    /**
     * 查询商品列表（支持分类筛选 + 关键词搜索 + 价格区间筛选）
     * @param categoryId 分类ID（可选）
     * @param keyword 搜索关键词（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @return 商品列表
     */
    @GetMapping("/list")
    public Result<List<Product>> listProduct(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return productService.searchProducts(categoryId, keyword, minPrice, maxPrice);
    }

    /**
     * 发布商品
     * @param product 商品信息（JSON格式）---->所以要用@RequestBody而不是@RequestParam
     * @param request 项目用的是 Header Token 认证方式，所以用于从请求头获取Token
     * @return 发布结果
     */
    @PostMapping("/publish")
    public Result<Void> publishProduct(@RequestBody Product product,
                                       HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);
        return productService.publishProduct(product, userId);
    }

    /**
     * 编辑商品
     * @param product 商品信息（JSON格式，包含商品ID和要修改的字段）
     * @param request 用于从请求头获取Token
     * @return 编辑结果
     */
    @PutMapping("/update")//----> PUT = 修改
    public Result<Void> updateProduct(@RequestBody Product product, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return productService.updateProduct(product, userId);
    }

    /**
     * 删除商品
     * @param id 要删除的商品ID
     * @param request 用于从请求头获取Token
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return productService.deleteProduct(id, userId);
    }

    /**
     * 查询我发布的商品列表
     * @param request 用于从请求头获取Token
     * @return 我发布的商品列表
     */
    @GetMapping("/my")
    public Result<List<Product>> getMyProducts(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return productService.getMyProducts(userId);
    }
}

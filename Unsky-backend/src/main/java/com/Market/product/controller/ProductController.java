package com.Market.product.controller;

import com.Market.common.entity.Product;
import com.Market.common.result.Result;
import com.Market.common.util.JwtUtil;
import com.Market.product.dto.ProductPublishDTO;
import com.Market.product.dto.ProductUpdateDTO;
import com.Market.product.query.ProductQuery;
import com.Market.product.service.ProductService;
import com.Market.product.vo.ProductDetailVO;
import com.Market.product.vo.ProductListVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    /**
     * 查询商品列表（支持分类筛选 + 关键词搜索 + 价格区间筛选）
     * 后面仍在进行功能的扩展和完善
     * @param productQuery 封装查询参数
     * @return 商品列表
     */
    @GetMapping("/list")
    public Result<IPage<ProductListVO>> listProduct(ProductQuery productQuery) {
        return productService.searchProducts(productQuery);
    }

    /**
     * 发布商品
     * @param productPublishDTO 商品信息（JSON格式）---->所以要用@RequestBody而不是@RequestParam
     * @param request 项目用的是 Header Token 认证方式，所以用于从请求头获取Token
     * @return 发布结果
     */
    @PostMapping("/publish")
    public Result<Void> publishProduct(@Valid @RequestBody ProductPublishDTO productPublishDTO,
                                       HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);
        return productService.publishProduct(productPublishDTO, userId);
    }

    /**
     * 编辑商品
     * @param productUpdateDTO 商品信息（JSON格式，包含商品ID和要修改的字段）
     * @param request 用于从请求头获取Token
     * @return 编辑结果
     */
    @PutMapping("/update")//----> PUT = 修改
    public Result<Void> updateProduct(@Valid @RequestBody ProductUpdateDTO productUpdateDTO, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return productService.updateProduct(productUpdateDTO, userId);
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

    /**
     * 热门商品展示
     * @param limit
     * @return
     */
    @GetMapping("/hot")
    public Result<List<ProductListVO>> listHotProducts(
            @RequestParam(required = false) Integer limit) {
        return productService.listHotProducts(limit);
    }

}

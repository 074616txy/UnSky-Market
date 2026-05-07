package com.Market.product.controller;

import com.Market.common.result.Result;
import com.Market.common.util.JwtUtil;
import com.Market.product.service.CartService;
import com.Market.product.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 加入购物车
     * @param productId
     * @param request
     * @return
     */
    @PostMapping("/add/{productId}")
    public Result<Void> addCart(@PathVariable Long productId,
                                HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return cartService.addCart(productId, userId);
    }

    /**
     * 删除购物车商品
     * @param productId
     * @param request
     * @return
     */
    @DeleteMapping("/remove/{productId}")
    public Result<Void> removeCart(@PathVariable Long productId,
                                   HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return cartService.removeCart(productId, userId);
    }

    /**
     * 我的购物车列表
     * @param request
     * @return
     */
    @GetMapping("/list")
    public Result<List<CartVO>> listMyCart(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return cartService.listMyCart(userId);
    }


}


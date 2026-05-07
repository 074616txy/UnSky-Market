package com.Market.product.controller;

import com.Market.common.result.Result;
import com.Market.common.util.JwtUtil;
import com.Market.product.service.FavoriteService;
import com.Market.product.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏商品
     * @param productId 收藏商品id
     * @param request  从请求头获取token
     * @return
     */
    @PostMapping("/add/{productId}")
    public Result<Void> addFavorite(@PathVariable Long productId,
                                    HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return favoriteService.addFavorite(productId, userId);
    }

    /**
     * 取消收藏
     * @param productId
     * @param request
     * @return
     */
    @DeleteMapping("/cancel/{productId}")
    public Result<Void> cancelFavorite(@PathVariable Long productId,
                                       HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return favoriteService.cancelFavorite(productId, userId);
    }

    /**
     * 我的收藏列表
     * @param request
     * @return
     */
    @GetMapping("/list")
    public Result<List<FavoriteVO>> listMyFavorites(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserIdFromToken(token);

        return favoriteService.listMyFavorites(userId);
    }


}

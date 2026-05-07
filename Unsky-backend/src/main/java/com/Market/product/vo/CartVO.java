package com.Market.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartVO {
    //购物车记录ID
    private Long cartId;
    //商品ID
    private Long productId;
    //商品标题
    private String title;
    //商品价格
    private BigDecimal price;
    //商品图片
    private String images;
    //商品浏览量
    private Integer viewCount;
    //加入购物车时间
    private LocalDateTime createTime;
}

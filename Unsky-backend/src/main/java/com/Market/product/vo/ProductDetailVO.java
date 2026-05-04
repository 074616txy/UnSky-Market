package com.Market.product.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class ProductDetailVO {

    // 商品ID
    private Long id;

    // 卖家ID
    private Long sellerId;

    // 商品标题
    private String title;

    // 商品描述（详情页展示完整描述）
    private String description;

    // 售价
    private BigDecimal price;

    // 原价
    private BigDecimal originalPrice;

    // 分类ID
    private Long categoryId;

    // 商品图片
    private List<String> images;

    // 浏览量
    private Integer viewCount;

    // 收藏量
    private Integer favoriteCount;

    // 新旧程度（1-5）
    private Byte conditionLevel;

    // 商品状态（1=上架）
    private Byte status;

    // 发布时间
    private LocalDateTime createTime;
}
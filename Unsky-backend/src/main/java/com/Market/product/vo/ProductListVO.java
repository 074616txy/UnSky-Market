package com.Market.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

    /**
     * 商品列表 VO（列表展示用）
     * 只包含前端展示需要的字段
     */
    @Data
    public class ProductListVO {

        // 商品ID
        private Long id;

        // 卖家ID
        private Long sellerId;

        // 商品标题
        private String title;

        // 商品描述（列表只展示前50字）
        private String description;

        // 售价
        private BigDecimal price;

        // 原价
        private BigDecimal originalPrice;

        // 分类ID
        private Long categoryId;

        // 商品图片（JSON转List）
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


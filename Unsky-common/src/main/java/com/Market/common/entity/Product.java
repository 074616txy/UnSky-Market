package com.Market.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类：对应数据库product
 */
@Data
@TableName("product")
public class Product {
        //商品ID
        @TableId(type = IdType.AUTO)
        private Long id;
        //卖家ID，对应 sys_user.id
        private Long sellerId;
        //分类ID，对应 product_category.id
        private Long categoryId;
        //商品标题
        private String title;
        //商品描述
        private String description;
        //商品售价
        private BigDecimal price;
        //商品原价
        private BigDecimal originalPrice;
        //新旧程度（1=较旧，5=几乎全新）
        private Byte conditionLevel;
        //商品图片JSON数组
        private String images;
        //浏览量
        private Integer viewCount;
        //收藏数量
        private Integer favoriteCount;
        //商品状态（0=下架，1=上架，2=已售）
        private Byte status;
        //创建时间
        private LocalDateTime createTime;
        //更新时间
        private LocalDateTime updateTime;
    }


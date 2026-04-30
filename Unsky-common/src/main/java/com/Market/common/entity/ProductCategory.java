package com.Market.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品实体类：对应数据库product_category
 */
@Data
@TableName("product_category")
public class ProductCategory {
    //主键 自增 分类ID
    @TableId(type = IdType.AUTO)
    private Long id;
    //分类名称
    private String name;
    //排序字段（值越小越靠前）
    private Integer sort;
    //状态（1=启用，0=禁用）
    private Byte status;
}

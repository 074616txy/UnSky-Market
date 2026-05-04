package com.Market.product.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class ProductUpdateDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long id;

    /**
     * 商品标题
     */
    @Size(max = 50, message = "商品标题不能超过50个字符")
    private String title;

    /**
     * 商品描述
     */
    @Size(max = 500, message = "商品描述不能超过500个字符")
    private String description;

    /**
     * 商品价格
     */
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    /**
     * 商品原价
     */
    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品成色
     */
    private Byte conditionLevel;

    /**
     * 商品图片
     */
    private String images;
}
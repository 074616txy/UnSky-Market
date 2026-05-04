package com.Market.product.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class ProductPublishDTO {

    /**
     * 商品标题
     */
    @NotBlank(message = "商品标题不能为空")
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
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    /**
     * 商品原价，可选
     */
    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;

    /**
     * 商品分类ID
     */
    @NotNull(message = "请选择商品分类")
    private Long categoryId;

    /**
     * 商品成色(1-5)
     */
    private Byte conditionLevel;

    /**
     * 商品图片，当前先用字符串保存
     * 后续如果实现多图上传，可以再改成 List<String>
     */
    private String images;
}

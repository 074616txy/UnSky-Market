package com.Market.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart {
    //购物车id
    private Long id;
    //用户id
    private Long userId;
    //商品id
    private Long productId;
    //加入购物车时间
    private LocalDateTime createTime;
}

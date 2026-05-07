package com.Market.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite")
public class Favorite {
    //收藏id
    private Long id;
    //用户id
    private Long userId;
    //商品id
    private Long productId;
    //收藏时间
    private LocalDateTime createTime;
}


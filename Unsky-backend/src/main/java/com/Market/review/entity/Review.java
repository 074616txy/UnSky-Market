package com.Market.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    // 评价ID
    private Long id;

    // 订单ID
    private Long orderId;

    // 商品ID
    private Long productId;

    // 评价人ID
    private Long fromUserId;

    // 被评价人ID
    private Long toUserId;

    // 评分：1-5分
    private Integer score;

    // 评价内容
    private String content;

    // 是否匿名：0不匿名，1匿名
    private Integer isAnonymous;

    // 创建时间
    private LocalDateTime createTime;
}

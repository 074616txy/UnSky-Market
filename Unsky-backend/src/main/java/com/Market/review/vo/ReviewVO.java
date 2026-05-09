package com.Market.review.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {

    // 评价ID
    private Long id;

    // 订单ID
    private Long orderId;

    // 商品ID
    private Long productId;

    // 商品标题
    private String productTitle;

    // 评价人ID
    private Long fromUserId;

    // 评价人昵称
    private String fromUsername;

    // 被评价人ID
    private Long toUserId;

    // 被评价人昵称
    private String toUsername;

    // 评分
    private Integer score;

    // 评分文本
    private String scoreText;

    // 评价内容
    private String content;

    // 是否匿名：0不匿名，1匿名
    private Integer isAnonymous;

    // 创建时间
    private LocalDateTime createTime;
}

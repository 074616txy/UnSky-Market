package com.Market.review.dto;

import lombok.Data;

@Data
public class AddReviewDTO {

    // 订单ID
    private Long orderId;

    // 评分：1-5分
    private Integer score;

    // 评价内容
    private String content;

    // 是否匿名：0不匿名，1匿名
    private Integer isAnonymous;
}

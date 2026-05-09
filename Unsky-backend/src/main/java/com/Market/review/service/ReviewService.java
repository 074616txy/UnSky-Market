package com.Market.review.service;

import com.Market.common.result.Result;
import com.Market.review.dto.AddReviewDTO;
import com.Market.review.vo.ReviewVO;

import java.util.List;

public interface ReviewService {



    /**
     * 发布评价
     * @param addReviewDTO
     * @param currentUserId
     * @return
     */
    Result<Void> addReview(AddReviewDTO addReviewDTO, Long currentUserId);

    /**
     * 查询用户收到的评价
     * @param userId
     * @return
     */
    Result<List<ReviewVO>> listReceivedReviews(Long userId);

    /**
     * 查询我发出的评价
     * @param currentUserId
     * @return
     */
    Result<List<ReviewVO>> listMySentReviews(Long currentUserId);



}

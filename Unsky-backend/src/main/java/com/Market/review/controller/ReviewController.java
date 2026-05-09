package com.Market.review.controller;

import com.Market.common.result.Result;
import com.Market.common.util.JwtUtil;
import com.Market.review.dto.AddReviewDTO;
import com.Market.review.service.ReviewService;
import com.Market.review.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 发布评价
     * @param addReviewDTO
     * @param request
     * @return
     */
    @PostMapping("/add")
    public Result<Void> addReview(@RequestBody AddReviewDTO addReviewDTO,
                                  HttpServletRequest request) {
        String token = request.getHeader("token");
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        return reviewService.addReview(addReviewDTO, currentUserId);
    }

    /**
     * 查询用户收到的评价
     * @param userId
     * @return
     */
    @GetMapping("/received/{userId}")
    public Result<List<ReviewVO>> listReceivedReviews(@PathVariable Long userId) {
        return reviewService.listReceivedReviews(userId);
    }

    /**
     * 查询我发出的评价
     * @param request
     * @return
     */
    @GetMapping("/sent")
    public Result<List<ReviewVO>> listMySentReviews(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        return reviewService.listMySentReviews(currentUserId);
    }




}

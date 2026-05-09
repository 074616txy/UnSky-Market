package com.Market.review.service.impl;

import com.Market.common.entity.Product;
import com.Market.common.entity.User;
import com.Market.common.result.Result;
import com.Market.order.constant.OrderStatus;
import com.Market.order.entity.Order;
import com.Market.order.mapper.OrderMapper;
import com.Market.product.mapper.ProductMapper;
import com.Market.review.constant.ReviewScore;
import com.Market.review.dto.AddReviewDTO;
import com.Market.review.entity.Review;
import com.Market.review.mapper.ReviewMapper;
import com.Market.review.service.ReviewService;
import com.Market.review.vo.ReviewVO;
import com.Market.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final OrderMapper orderMapper;
    private final ReviewMapper reviewMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;


    /**
     * 发布评价
     * @param addReviewDTO
     * @param currentUserId
     * @return
     */
    @Override
    public Result<Void> addReview(AddReviewDTO addReviewDTO, Long currentUserId) {
        // 1. 判断订单ID是否为空
        if (addReviewDTO == null || addReviewDTO.getOrderId() == null) {
            return Result.error("订单ID不能为空");
        }

        // 2. 查询订单是否存在
        Order order = orderMapper.selectById(addReviewDTO.getOrderId());
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 3. 判断订单是否已完成
        if (!order.getStatus().equals(OrderStatus.FINISHED)) {
            return Result.error("只有已完成订单才能评价");
        }

        // 4. 判断当前用户是否是订单参与者，并确定被评价人
        Long toUserId;

        if (order.getBuyerId().equals(currentUserId)) {
            // 当前用户是买家，被评价人是卖家
            toUserId = order.getSellerId();
        } else if (order.getSellerId().equals(currentUserId)) {
            // 当前用户是卖家，被评价人是买家
            toUserId = order.getBuyerId();
        } else {
            return Result.error("只能评价自己参与的订单");
        }

        // 5. 判断是否已经评价过
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, order.getId())
                .eq(Review::getFromUserId, currentUserId);

        Long count = reviewMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("请勿重复评价");
        }

        // 6. 判断评分是否合法
        if (addReviewDTO.getScore() == null
                || addReviewDTO.getScore() < ReviewScore.MIN_SCORE
                || addReviewDTO.getScore() > ReviewScore.MAX_SCORE) {
            return Result.error("评分必须在1到5之间");
        }

        // 7. 创建评价对象
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setProductId(order.getProductId());
        review.setFromUserId(currentUserId);
        review.setToUserId(toUserId);
        review.setScore(addReviewDTO.getScore());
        review.setContent(addReviewDTO.getContent());
        review.setIsAnonymous(addReviewDTO.getIsAnonymous() == null ? 0 : addReviewDTO.getIsAnonymous());

        reviewMapper.insert(review);

        // 8. 更新被评价人的信用分
        User toUser = userMapper.selectById(toUserId);
        if (toUser != null) {
            Integer currentScore = toUser.getCreditScore() == null ? 100 : toUser.getCreditScore();
            Integer changeScore = calculateCreditChange(addReviewDTO.getScore());

            toUser.setCreditScore(currentScore + changeScore);
            userMapper.updateById(toUser);
        }
        return Result.success();
    }

    /**
     * 查询用户收到的评价
     * @param userId
     * @return
     */
    @Override
    public Result<List<ReviewVO>> listReceivedReviews(Long userId) {
        // 1. 判断用户ID是否为空
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }

        // 2. 查询该用户收到的评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getToUserId, userId);
        wrapper.orderByDesc(Review::getCreateTime);

        List<Review> reviewList = reviewMapper.selectList(wrapper);

        // 3. 转换为VO
        List<ReviewVO> voList = reviewList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    // 转换评价VO
    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();

        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setProductId(review.getProductId());
        vo.setFromUserId(review.getFromUserId());
        vo.setToUserId(review.getToUserId());
        vo.setScore(review.getScore());
        vo.setScoreText(getScoreText(review.getScore()));
        vo.setContent(review.getContent());
        vo.setIsAnonymous(review.getIsAnonymous());
        vo.setCreateTime(review.getCreateTime());

        // 查询商品标题
        Product product = productMapper.selectById(review.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
        }

        // 查询评价人昵称
        User fromUser = userMapper.selectById(review.getFromUserId());
        if (fromUser != null) {
            if (review.getIsAnonymous() != null && review.getIsAnonymous() == 1) {
                vo.setFromUsername("匿名用户");
            } else {
                vo.setFromUsername(fromUser.getNickname());
            }
        }

        // 查询被评价人昵称
        User toUser = userMapper.selectById(review.getToUserId());
        if (toUser != null) {
            vo.setToUsername(toUser.getNickname());
        }

        return vo;
    }

    // 转换评分文本
    private String getScoreText(Integer score) {
        if (score == null) {
            return "未知评价";
        }

        if (score >= 5) {
            return "好评";
        } else if (score >= 3) {
            return "中评";
        } else {
            return "差评";
        }
    }

    /**
     * 查询我发出的评价
     * @param currentUserId
     * @return
     */
    @Override
    public Result<List<ReviewVO>> listMySentReviews(Long currentUserId) {
        // 1. 查询当前用户发出的评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getFromUserId, currentUserId);
        wrapper.orderByDesc(Review::getCreateTime);

        List<Review> reviewList = reviewMapper.selectList(wrapper);

        // 2. 转换为VO
        List<ReviewVO> voList = reviewList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    // 根据评分计算信用分变化值
    private Integer calculateCreditChange(Integer score) {
        if (score == null) {
            return 0;
        }

        switch (score) {
            case 5:
                return 2;
            case 4:
                return 1;
            case 3:
                return 0;
            case 2:
                return -1;
            case 1:
                return -2;
            default:
                return 0;
        }
    }





}


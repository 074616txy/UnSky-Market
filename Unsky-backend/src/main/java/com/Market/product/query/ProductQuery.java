package com.Market.product.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品查询条件封装
 * 用于接收前端传入的查询参数
 *  示  - * @param categoryId 分类ID（可选，传null表示不限制）
 *  例  - * @param keyword    搜索关键词（可选，传null表示查全部）
 *  展  - * @param minPrice   最低价格（可选，传null表示不限制最低价）
 *  示  - * @param maxPrice   最高价格（可选，传null表示不限制最高价）
 *  ！  - * @param pageNum    当前页码（默认第1页）
 *  ！  - * @param pageSize   每页条数（默认10条）
 *  ！  - * @param sortBy     排序字段（可选）
 */
@Data
public class ProductQuery {

    // 分类ID（可选）
    private Long categoryId;

    // 搜索关键词（可选）
    private String keyword;

    // 最低价格（可选）
    private BigDecimal minPrice;

    // 最高价格（可选）
    private BigDecimal maxPrice;

    //分页参数（带默认值）
    private Integer pageNum;//默认第一页
    private Integer pageSize;//默认每页10条

    // 排序字段（可选）
    // 可选值：price_asc（价格升序）、price_desc（价格降序）、
    //        view_count（浏览量降序）、create_time_desc（最新优先）
    private String sortBy;
}
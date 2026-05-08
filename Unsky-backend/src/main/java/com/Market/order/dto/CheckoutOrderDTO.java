package com.Market.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutOrderDTO {

    // 要结算的购物车ID列表
    private List<Long> cartIds;
}

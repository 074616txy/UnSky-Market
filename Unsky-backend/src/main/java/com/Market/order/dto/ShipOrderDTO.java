package com.Market.order.dto;

import lombok.Data;

@Data
public class ShipOrderDTO {

    // 订单ID
    private Long orderId;

    // 物流单号
    private String logisticsNo;
}

package com.fashion_store.dto.DashboardSummary.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardSummaryResponse {
    Integer totalOrder;
    Integer totalPost;
    Integer totalCustomer;
    BigDecimal totalRevenue;

    Integer totalOrderPrev;
    Integer totalPostPrev;
    Integer totalCustomerPrev;
    BigDecimal totalRevenuePrev;

    List<BigDecimal> monthlyRevenue;
    List<BigDecimal> monthlyRevenuePrev;

    Map<String, Long> topCategory;

    List<CustomerDashboardResponse> customerList;
    List<OrderDashboardResponse> orderList;
}

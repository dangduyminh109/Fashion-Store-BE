package com.fashion_store.dto.DashboardSummary.response;

import com.fashion_store.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDashboardResponse {
    String customerName;
    Date createdAt;
    OrderStatus orderStatus;
    BigDecimal totalAmount;
    Integer numOfProduct;
}

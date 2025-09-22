package com.fashion_store.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {
    @NotNull(message = "INVALID_SKU")
    String sku;
    @NotNull(message = "INVALID_QUANTITY")
    Integer quantity;
}
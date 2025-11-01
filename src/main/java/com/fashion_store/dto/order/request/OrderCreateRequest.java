package com.fashion_store.dto.order.request;


import com.fashion_store.validator.PaymentMethodConstraint;
import com.fashion_store.validator.PhoneConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreateRequest {
    @NotBlank(message = "INVALID_NAME")
    String customerName;
    @NotBlank(message = "INVALID_PHONE")
    @PhoneConstraint
    String phone;
    @NotBlank(message = "INVALID_ADDRESS")
    String address;
    @NotBlank(message = "INVALID_CITY")
    String city;
    @NotBlank(message = "INVALID_DISTRICT")
    String district;
    @NotNull(message = "INVALID_WARD")
    String ward;
    @NotNull(message = "INVALID_CITY")
    Integer cityId;
    @NotNull(message = "INVALID_DISTRICT")
    Integer districtId;
    @NotNull(message = "INVALID_WARD")
    Integer wardId;

    String note;
    String customerId;
    Long voucherId;
    @PaymentMethodConstraint
    @NotNull(message = "INVALID_PAYMENT_METHOD")
    String paymentMethod;
    @Valid
    @Size(min = 1, message = "INVALID_ORDER_ITEM_LIST")
    @NotNull(message = "INVALID_ORDER_ITEM_LIST")
    List<OrderItemRequest> orderItems;
}
package com.fashion_store.dto.order.request;

import com.fashion_store.validator.PaymentMethodConstraint;
import com.fashion_store.validator.PhoneConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderUpdateRequest {
    String customerName;
    @PhoneConstraint
    String phone;
    String address;
    String city;
    String district;
    String ward;
    String note;
    String customerId;
    Long voucherId;
    @PaymentMethodConstraint
    String paymentMethod;
    String orderStatus;
    Boolean isPaid;
}

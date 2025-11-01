package com.fashion_store.dto.address.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {
    @NotNull(message = "INVALID_CUSTOMER_ID")
    String customerId;
    @NotBlank(message = "INVALID_NAME")
    String name;
    @NotBlank(message = "INVALID_ADDRESS")
    String address;
    @NotBlank(message = "INVALID_PHONE")
    String phone;
    @NotBlank(message = "INVALID_CITY")
    String city;
    @NotBlank(message = "INVALID_DISTRICT")
    String district;
    @NotBlank(message = "INVALID_WARD")
    String ward;

    @NotNull(message = "INVALID_CITY")
    Integer cityId;
    @NotNull(message = "INVALID_DISTRICT")
    Integer districtId;
    @NotNull(message = "INVALID_WARD")
    Integer wardId;

    @Builder.Default
    Boolean isDefault = false;
}
package com.fashion_store.dto.address.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressUpdateRequest {
    @NotNull(message = "INVALID_CUSTOMER_ID")
    String customerId;
    @Size(min = 1, message = "INVALID_NAME")
    String name;
    @Size(min = 1, message = "INVALID_ADDRESS")
    String address;
    @Size(min = 1, message = "INVALID_PHONE")
    String phone;
    @Size(min = 1, message = "INVALID_CITY")
    String city;
    @Size(min = 1, message = "INVALID_DISTRICT")
    String district;
    @Size(min = 1, message = "INVALID_WARD")
    String ward;
    @NotNull(message = "INVALID_CITY")
    Integer cityId;
    @NotNull(message = "INVALID_DISTRICT")
    Integer districtId;
    @NotNull(message = "INVALID_WARD")
    Integer wardId;
    Boolean isDefault;
}
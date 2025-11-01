package com.fashion_store.dto.address.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    Long id;
    String customerId;
    String name;
    String address;
    String phone;
    String city;
    String district;
    String ward;
    Boolean isDefault;
    Integer cityId;
    Integer districtId;
    Integer wardId;
}

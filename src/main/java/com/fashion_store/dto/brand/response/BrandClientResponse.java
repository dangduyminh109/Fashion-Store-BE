package com.fashion_store.dto.brand.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BrandClientResponse {
    Long id;
    String name;
    String slug;
    String image;
}

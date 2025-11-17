package com.fashion_store.dto.product.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    String Name;
    String Description;
    String Slug;
    String Brand;
    String Images;
    String Category;
    BigDecimal Price;
}

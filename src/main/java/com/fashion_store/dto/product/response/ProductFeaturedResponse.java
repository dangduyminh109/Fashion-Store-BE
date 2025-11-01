package com.fashion_store.dto.product.response;

import com.fashion_store.dto.variant.response.VariantResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductFeaturedResponse {
    Long id;
    String name;
    Boolean isFeatured;
    String brandName;
    String categoryName;
    String slug;
    List<String> productImages;
    List<VariantResponse> variants;
}

package com.fashion_store.dto.category.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryFeaturedResponse {
    Long id;
    String name;
    String slug;
    String image;
    Integer productCount;
}

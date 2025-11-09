package com.fashion_store.dto.product.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductClientResponse {
    List<ProductResponse> listProduct;
    Integer totalPage;
}

package com.fashion_store.dto.product.request;

import com.fashion_store.dto.variant.request.VariantUpdateRequest;
import com.fashion_store.validator.VariantListConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    @NotBlank(message = "INVALID_NAME")
    String name;

    String description;
    Boolean status;

    Long brandId;
    Long categoryId;

    Boolean isFeatured;

    @NotNull(message = "INVALID_VARIANT")
    @Size(min = 1, message = "INVALID_VARIANT")
    @VariantListConstraint(message = "INVALID_ATTRIBUTE_COUNT")
    @Valid
    List<VariantUpdateRequest> variantList;

    List<MultipartFile> images;

    List<String> listDeletedImage;
}

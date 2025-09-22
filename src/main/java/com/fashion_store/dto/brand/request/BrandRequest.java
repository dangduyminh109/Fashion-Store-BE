package com.fashion_store.dto.brand.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BrandRequest {
    @NotBlank(message = "INVALID_NAME")
    String name;
    @Builder.Default
    Boolean status = true;

    MultipartFile image;

    @Builder.Default
    Boolean imageDelete = false;
}

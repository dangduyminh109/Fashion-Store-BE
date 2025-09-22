package com.fashion_store.dto.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryRequest {
    @NotBlank(message = "INVALID_NAME")
    String name;
    Long parentId;
    @Builder.Default
    Boolean status = true;
    MultipartFile image;
    @Builder.Default
    Boolean imageDelete = false;
}

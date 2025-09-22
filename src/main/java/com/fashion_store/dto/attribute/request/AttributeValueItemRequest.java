package com.fashion_store.dto.attribute.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttributeValueItemRequest {
    @NotBlank(message = "INVALID_VALUE")
    String value;
    Long id;
    String color;
    MultipartFile image;
    Boolean imageDelete;
}

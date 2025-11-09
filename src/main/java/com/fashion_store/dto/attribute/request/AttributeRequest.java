package com.fashion_store.dto.attribute.request;

import com.fashion_store.validator.DisplayTypeConstraint;
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
public class AttributeRequest {
    @NotBlank(message = "INVALID_NAME")
    String name;

    @DisplayTypeConstraint
    @Builder.Default
    String displayType = "text";

    @Builder.Default
    Boolean status = true;

    @Size(min = 1, message = "INVALID_ATTRIBUTE_COUNT")
    @NotNull(message = "INVALID_ATTRIBUTE_COUNT")
    @Valid
    List<AttributeValueItemRequest> listAttributeValue;
}

package com.fashion_store.dto.role.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
    @NotBlank(message = "INVALID_NAME")
    String name;
    String description;
    @Builder.Default
    Boolean status = true;
}

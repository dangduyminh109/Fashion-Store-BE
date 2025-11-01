package com.fashion_store.dto.customer.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateInfoRequest {
    @NotBlank(message = "INVALID_NAME")
    String fullName;
    @Email(message = "INVALID_EMAIL")
    String email;
    String phone;
    MultipartFile avatar;
    @Builder.Default
    Boolean avatarDelete = false;
}

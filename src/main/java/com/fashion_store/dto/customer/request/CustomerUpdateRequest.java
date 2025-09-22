package com.fashion_store.dto.customer.request;

import com.fashion_store.validator.AuthProviderConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest {
    @NotBlank(message = "INVALID_NAME")
    String fullName;
    @Email(message = "INVALID_EMAIL")
    String email;
    String phone;
    Boolean isGuest;
    @Size(min = 6, max = 32, message = "PASSWORD_LENGTH_INVALID")
    String newPassword;
    String providerId;
    Boolean status;

    @AuthProviderConstraint(message = "INVALID_AUTH_PROVIDER")
    String authProvider;

    MultipartFile avatar;

    @Builder.Default
    Boolean avatarDelete = false;
}

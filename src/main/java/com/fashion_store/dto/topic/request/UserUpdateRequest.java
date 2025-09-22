package com.fashion_store.dto.topic.request;

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
public class UserUpdateRequest {
    @NotBlank(message = "INVALID")
    String username;
    @Size(min = 6, max = 32, message = "PASSWORD_LENGTH_INVALID")
    String password;
    String firstName;
    String lastName;
    String phone;
    Boolean status;

    @Email(message = "INVALID_EMAIL")
    String email;

    Long roleId;

    MultipartFile avatar;

    @Builder.Default
    Boolean avatarDelete = false;
}

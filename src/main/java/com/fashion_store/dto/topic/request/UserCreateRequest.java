package com.fashion_store.dto.topic.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    @NotBlank(message = "INVALID")
    String username;
    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 32, message = "PASSWORD_LENGTH_INVALID")
    String password;
    String firstName;
    String lastName;
    String phone;

    @Email(message = "INVALID_EMAIL")
    @Column(unique = true)
    String email;

    Long roleId;

    MultipartFile avatar;

    @Builder.Default
    Boolean status = true;
}




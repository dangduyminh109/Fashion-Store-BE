package com.fashion_store.dto.customer.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerForgotPasswordRequest {
    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_REQUIRED")
    String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 32, message = "PASSWORD_LENGTH_INVALID")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "PASSWORD_WEAK"
    )
    String newPassword;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 32, message = "PASSWORD_LENGTH_INVALID")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "PASSWORD_WEAK"
    )
    String confirmPassword;

    @NotBlank(message = "INVALID_OTP")
    String otp;
}

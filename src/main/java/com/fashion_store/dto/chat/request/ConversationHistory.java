package com.fashion_store.dto.chat.request;

import com.fashion_store.validator.ChatRoleConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationHistory {
    @ChatRoleConstraint
    @Builder.Default
    String role = "CUSTOMER";

    @NotBlank(message = "INVALID_MESSAGE")
    String message;
}

package com.fashion_store.validator;

import com.fashion_store.enums.ChatRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChatRoleValidator implements ConstraintValidator<ChatRoleConstraint, String> {
    @Override
    public boolean isValid(String ChatRoleType, ConstraintValidatorContext constraintValidatorContext) {
        if (ChatRoleType == null) {
            return true;
        }
        try {
            ChatRole.valueOf(ChatRoleType.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

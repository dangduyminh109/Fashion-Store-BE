package com.fashion_store.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ChatRoleValidator.class})
public @interface ChatRoleConstraint {
    String message() default "INVALID_CHAT_ROLE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

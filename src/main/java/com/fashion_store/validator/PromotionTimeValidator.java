package com.fashion_store.validator;

import com.fashion_store.dto.variant.request.VariantCreateRequest;
import com.fashion_store.dto.variant.request.VariantUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PromotionTimeValidator implements ConstraintValidator<PromotionTimeConstraint, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value instanceof VariantCreateRequest request) {
            if (request.getPromotionStartTime() != null && request.getPromotionEndTime() != null)
                return request.getPromotionEndTime().isAfter(request.getPromotionStartTime());
        }
        if (value instanceof VariantUpdateRequest request) {
            if (request.getPromotionStartTime() != null && request.getPromotionEndTime() != null)
                return request.getPromotionEndTime().isAfter(request.getPromotionStartTime());
        }
        return true;
    }
}

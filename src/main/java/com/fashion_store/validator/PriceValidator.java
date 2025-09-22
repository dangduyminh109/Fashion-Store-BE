package com.fashion_store.validator;

import com.fashion_store.dto.variant.request.VariantCreateRequest;
import com.fashion_store.dto.variant.request.VariantUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class PriceValidator implements ConstraintValidator<PriceConstraint, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        if (value instanceof VariantCreateRequest) {
            VariantCreateRequest request = (VariantCreateRequest) value;
            BigDecimal original = request.getOriginalPrice();
            BigDecimal sale = request.getSalePrice();
            BigDecimal promo = request.getPromotionalPrice();
            boolean isValid = true;
            if (original != null && original.compareTo(BigDecimal.ZERO) < 0) {
                isValid = false;
            }
            if (sale == null || sale.compareTo(BigDecimal.ZERO) < 0) {
                isValid = false;
            }
            if (promo != null && promo.compareTo(BigDecimal.ZERO) < 0) {
                isValid = false;
            }
            if (promo != null && sale != null && promo.compareTo(sale) > 0) {
                isValid = false;
            }
            return isValid;
        }
        if (value instanceof VariantUpdateRequest) {
            VariantUpdateRequest request = (VariantUpdateRequest) value;
            BigDecimal original = request.getOriginalPrice();
            BigDecimal sale = request.getSalePrice();
            BigDecimal promo = request.getPromotionalPrice();
            boolean isValid = true;
            if (original != null && original.compareTo(BigDecimal.ZERO) < 0) {
                isValid = false;
            }
            if (sale == null || sale.compareTo(BigDecimal.ZERO) < 0) {
                isValid = false;
            }
            if (promo != null && promo.compareTo(BigDecimal.ZERO) < 0) {
                isValid = false;
            }
            if (promo != null && sale != null && promo.compareTo(sale) > 0) {
                isValid = false;
            }
            return isValid;
        }

        return true;
    }
}

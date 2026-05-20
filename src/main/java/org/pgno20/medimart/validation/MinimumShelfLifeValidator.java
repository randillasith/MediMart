package org.pgno20.medimart.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class MinimumShelfLifeValidator implements ConstraintValidator<MinimumShelfLife, LocalDate> {

    private int months;

    @Override
    public void initialize(MinimumShelfLife constraintAnnotation) {
        this.months = constraintAnnotation.months();
    }

    @Override
    public boolean isValid(LocalDate expiryDate, ConstraintValidatorContext context) {
        if (expiryDate == null) {
            return true; // Use @NotNull if null is not allowed
        }
        LocalDate minimumValidDate = LocalDate.now().plusMonths(months);
        return !expiryDate.isBefore(minimumValidDate);
    }
}

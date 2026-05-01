package org.pgno20.medimart.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MinimumShelfLifeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinimumShelfLife {
    String message() default "Expiry date must be at least {months} months in the future";
    int months() default 3;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

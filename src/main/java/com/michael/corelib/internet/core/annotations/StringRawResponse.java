package com.michael.corelib.internet.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by michael on 15/2/11.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface StringRawResponse {
    String value() default "";
}

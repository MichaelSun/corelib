/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.michael.corelib.internet.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reset method full url path
 * 
 * @author michael
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface RestMethodUrl {
	String value() default "";
}

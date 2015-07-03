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
 * 
 * Annotation used for REST request. All Requests inherit from
 * {@link RequestBase} MUST add Annotation (either this or {@link RequiredParam}
 * ) to their declared fields that should be send to the REST server.
 * 
 * Fields that annotated as OptionalParam will be send to the REST server
 * without NULL check.
 * 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface OptionalParam {
	String value() default "";
}

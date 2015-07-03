/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */
package com.michael.corelib.internet.core.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * JsonProperty, can be used in JsonCreator parameters and declared fields. Java
 * Bean annotated with this annotation will be filled with value from json
 * string automatically.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface JsonProperty {

	/**
	 * key in json value
	 * 
	 * @return
	 */
	String value() default "";
}

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
 * JsonCreator, in order to declare constructor that use @JsonProperty as
 * parameters.
 * 
 * Java Bean with multiple JsonCreators, the last one will be used.
 * 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR })
public @interface JsonCreator {
	// No value
}

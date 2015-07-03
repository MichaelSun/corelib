/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */
package com.michael.corelib.internet.core.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 * Bean Description
 * Describe bean's fields and constructor, etc.
 * 
 *
 */
public class BeanDescription {
	
	/**
	 * whether class has a JsonCreator
	 */
	boolean hasJsonCreator;
	
	/**
	 * whether class is primitive
	 */
	boolean isPrimitive;
	
	/**
	 * constructor used to instantiate the object, may be a JsonCreator
	 */
	Constructor<?> constructor;
	
	/**
	 * field descriptions
	 */
	ArrayList<FieldDescription> fieldDescriptions;
	
	/**
	 * constructor parameter descriptions
	 */
	ArrayList<ConstructorParamDescription> constructorParamDescriptions;
	
	/**
	 * Add a field description
	 * @param fieldDescription
	 * 			field description to add
	 */
	public void addFieldDescription (FieldDescription fieldDescription) {
		if (fieldDescriptions == null) {
			fieldDescriptions = new ArrayList<FieldDescription>();
		}
		if (fieldDescription != null) {
    		fieldDescriptions.add(fieldDescription);
		}
		
	}
	
	/**
	 * Add a field description
	 * @param key
	 * 			key in json
	 * @param type
	 * 			field type
	 * @param value
	 * 			field value
	 * @param name
	 * 			field name
	 * @param genericType
	 * 			field generic type, use in List
	 */
	public void addFieldDescription (String key, Class<?> type, Object value, String name, Type genericType) {
		FieldDescription fieldDescription = new FieldDescription(key, type, value, name, genericType);
		this.addFieldDescription(fieldDescription);
	}
	
	/**
	 * Add a constructor parameter description
	 * @param constructorParamDescription
	 * 			constructor parameter description to add
	 */
	public void addConstructorParamDescription (ConstructorParamDescription constructorParamDescription) {
		if (constructorParamDescriptions == null) {
			constructorParamDescriptions = new ArrayList<ConstructorParamDescription>();
		}
		if (constructorParamDescription != null) {
			constructorParamDescriptions.add(constructorParamDescription);
		}
	}
	
	/**
	 * Add a constructor parameter array
	 * @param cpdArray
	 * 			constructor parameter array to add
	 */
	public void addConstructorParamDescription (ConstructorParamDescription[] cpdArray) {
		
		if (cpdArray != null) {
			if (constructorParamDescriptions == null) {
				constructorParamDescriptions = new ArrayList<ConstructorParamDescription>();
			}
			constructorParamDescriptions.addAll(Arrays.asList(cpdArray));
		}
		
	}
	
	public String toString () {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("field desp: ").append("\n");
		if (fieldDescriptions == null) {
			sb.append("\tnull").append("\n");
		} else {
			for (FieldDescription fd : fieldDescriptions) {
				sb.append(fd.toString()).append("\n");
			}
		}
		
		sb.append("con desp:").append("\n");
		if (constructorParamDescriptions == null) {
			sb.append("\tnull").append("\n");
		} else {
			for (ConstructorParamDescription cpd : constructorParamDescriptions) {
				sb.append(cpd.toString()).append("\n");
			} 
		}
		
		return sb.toString();
	}
	    	
	/**
	 * 
	 * Field Description
	 *
	 */
	public static class FieldDescription {
    	
		/**
		 * key in json value
		 */
    	public String key;
    	
    	/**
    	 * field type
    	 */
    	public Class<?> type;
    	
    	/**
    	 * field value
    	 */
    	public Object value;
    	
    	/**
    	 * field name
    	 */
    	public String name;
    	
    	/**
    	 * generic type
    	 */
    	public Type genericType;
    	
    	/**
    	 * field reference
    	 */
    	public Field field;
    	
    	public FieldDescription (String key, Class<?> type, Object value, String name, Type genericType) {
    		this.key = key;
    		this.type = type;
    		this.value = value;
    		this.name = name;
    		this.genericType = genericType;
    	}
    	
    	public String toString () {
    		return "key :" + key + " type :" + type + " value:" + value;
    	}
    	
	}
	
	/**
	 * 
	 * Constructor Parameter Description
	 *
	 */
	public static class ConstructorParamDescription {
		
		/**
		 * is this parameter annotated with JsonProperty
		 */
		public boolean isJsonProperty;
		
		/**
		 * parameter type
		 */
		public Class<?> type;
		
		/**
		 * parameter value
		 */
		public Object value;
		
		/**
		 * link with field description
		 */
		public FieldDescription fieldDescription;
		
		public ConstructorParamDescription (boolean isJsonProperty, Class<?> type, Object value) {
			this.isJsonProperty = isJsonProperty;
			this.type = type;
			this.value = value;
		}
		
		public String toString () {
			return "isJsonProperty :" + isJsonProperty + " type :" + type + " value :" + value; 
		}
		
	}

}

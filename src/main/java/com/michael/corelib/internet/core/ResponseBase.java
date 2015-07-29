/*
 * Copyright (C) 2014 michaelsun.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michael.corelib.internet.core;

import com.michael.corelib.internet.core.json.JsonProperty;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public abstract class ResponseBase {

	public String originJsonString;

	/**
	 * 
	 * print fields in response bean
	 * 
	 * @param level
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String toString (int level) {

		StringBuffer sb = new StringBuffer();
		Class<?> c = this.getClass();
		Field[] fields = c.getDeclaredFields();
		StringBuffer prefix = new StringBuffer();
		if (level > 0) {
			for (int i = 0 ; i < level ; i ++ ) {
				prefix.append("     ");
			}
			prefix.append("|----");
		}
		sb.append(prefix + "--------------begin element--------------\r\n");
		for (Field field : fields) {
			
			field.setAccessible(true);
			String key = field.getName();
			if (field.isAnnotationPresent(JsonProperty.class)) {
				JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
				key = jsonProperty.value();
				
			}
			
			sb.append(prefix).append(key).append(" = ");
			try {
				Class<?> type = field.getType();
				Object obj = field.get(this);
				if (obj == null) {
					sb.append("\r\n");
					continue;
				}
				if (type.isArray()) {
					int length = Array.getLength(obj);
					sb.append("[");
					for (int i = 0 ; i < length ; i ++ ) {
						Object item = Array.get(obj, i);
						sb.append(item).append(",");
					}
					if (length > 0) {
						sb.deleteCharAt(sb.length() - 1);
					}
					sb.append("]");
					sb.append("\r\n");
				} else {
					if (List.class.isInstance(obj)) {
						sb.append("\r\n");
						List list = (List) obj;
						Iterator iterator = list.iterator();
						while (iterator.hasNext()) {
							Object o = iterator.next();
							String value = "";
							if (o instanceof ResponseBase) {
								Method toStringMethod = o.getClass().getSuperclass().getDeclaredMethod("toString", int.class);
								value = (String) toStringMethod.invoke(o, level + 1);
							} else {
								value = String.valueOf(o.toString());
							}	
							sb.append(value).append("\r\n");
						}
					} else {
						String value = "";
						if (obj instanceof ResponseBase) {
							sb.append("\r\n");
							Method toStringMethod = obj.getClass().getSuperclass().getDeclaredMethod("toString", int.class);
							value = (String) toStringMethod.invoke(obj, level + 1);
						} else {
							value = String.valueOf(obj);
						}	
						sb.append(value).append("\r\n");
					}
				}
			
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			
		}
		sb.append(prefix + "--------------end element--------------\r\n");
		
		return sb.toString();
	
	}

	public String toString () {
		return toString (0);
	}

}

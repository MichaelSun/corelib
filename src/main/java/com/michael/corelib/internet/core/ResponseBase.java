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

public abstract class ResponseBase {

	public NetworkResponse networkResponse;

    private static final String NEW_LINE = "\n";

    private static final String DEFAULT_INDENT = "   ";
    private static final String OBJECT_START = "{";
    private static final String OBJECT_END = "}";
    private static final String ARRRY_START = "[";
    private static final String ARRAY_END = "]";
    private static final String KEY_VALUE_SPLITOR = " : ";
    private static final String END_OBJECT = ",";

	public String toPrettyJSon() {
		StringBuilder sb = new StringBuilder();
        sb.append(NEW_LINE);
        writeObjectPretty(sb, this, "");
        sb.append(NEW_LINE);

        return sb.toString();
	}

	private void writeObjectPretty(StringBuilder outSB, Object object, String indent) {
        if (outSB == null) {
            return;
        }

        if (object == null) {
            outSB.append("null");
            return;
        }
        outSB.append(OBJECT_START).append(NEW_LINE);
        Class<?> c = object.getClass();
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(JsonProperty.class)) {
                continue;
            }

            field.setAccessible(true);
            String key = field.getName();
            try {
                Class<?> type = field.getType();
                if (type.isPrimitive() || type == String.class) {
                    outSB.append(indent + DEFAULT_INDENT).append(key).append(KEY_VALUE_SPLITOR).append(String.valueOf(field.get(object))).append(END_OBJECT).append(NEW_LINE);
                } else if (type.isArray()) {
                    outSB.append(indent + DEFAULT_INDENT).append(key).append(KEY_VALUE_SPLITOR);
                    writeArrayValuePretty(outSB, type, field.get(object), indent + DEFAULT_INDENT);
                } else {
                    outSB.append(indent + DEFAULT_INDENT).append(key).append(KEY_VALUE_SPLITOR);
                    writeObjectPretty(outSB, field.get(object), indent + DEFAULT_INDENT);
                    outSB.append(NEW_LINE);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        outSB.append(indent).append(OBJECT_END);
    }

    private void writeArrayValuePretty(StringBuilder outSB, Class<?> arrayType, Object obj, String indent) {
        if (arrayType == null || !arrayType.isArray() || obj == null) {
            return;
        }

        int length = Array.getLength(obj);
        if (length == 0) {
            outSB.append("null").append(NEW_LINE);
            return;
        }
        boolean isFristOne = true;
        boolean isObjectArray = false;
        outSB.append(ARRRY_START);
        for (int i = 0 ; i < length ; i ++ ) {
            Object item = Array.get(obj, i);
            if (item.getClass().isPrimitive()
                    || item.getClass() == String.class
                    ||  item.getClass() == Integer.class
                    ||  item.getClass() == Float.class
                    ||  item.getClass() == Double.class
                    ||  item.getClass() == Long.class) {
                outSB.append(String.valueOf(item));
            } else {
                if (isFristOne) {
                    isFristOne = false;
                    outSB.append(NEW_LINE);
                }
                isObjectArray = true;
                outSB.append(indent + DEFAULT_INDENT);
                writeObjectPretty(outSB, item, indent + DEFAULT_INDENT);
            }

            if (i != (length -1)) {
                outSB.append(END_OBJECT);
            }
            if (isObjectArray) {
                outSB.append(NEW_LINE);
            }
        }

        if (isObjectArray) {
            outSB.append(indent);
        }
        outSB.append(ARRAY_END).append(NEW_LINE);
    }

	/**
	 * 
	 * print fields in response bean
	 * 
	 * @param level
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String toString (int level) {

        return toPrettyJSon();
/**
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
	*/
	}

    @Override
	public String toString () {
		return toString (0);
	}
}

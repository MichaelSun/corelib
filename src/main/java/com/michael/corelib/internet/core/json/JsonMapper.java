/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */
package com.michael.corelib.internet.core.json;

import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * convert Json String to Java Bean
 */
public class JsonMapper {


    /**
     * Entry. convert content to bean.
     *
     * @param content   Json content
     * @param valueType Dest value type
     * @return Object instaceof Dest value type.
     * @throws org.json.JSONException
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(String content, Class<T> valueType) throws JSONException {

        LOGD("[[content]]" + content);

        if (valueType == null) {
            throw new IllegalArgumentException("ValueType MUST NOT BE NULL");
        }

        Object instance = null;
        if (valueType.isArray()) {
            //TODO value type is array
            //Not support yet..
            throw new RuntimeException("Not support Yet...");

        } else {

            JSONObject root = new JSONObject(content);

            //get bean description
            BeanDescription beanDescription = getBeanDescription(valueType);

            //fill bean description with value
            processMapping(beanDescription, root);

            //fill java bean with bean description
            instance = fillInstance(beanDescription);


        }

        return (T) instance;
    }

    /**
     * Get bean description from value type
     *
     * @param valueType value type
     * @return bean description
     */
    private <T> BeanDescription getBeanDescription(Class<T> valueType) {

        //try primitive type first
        BeanDescription beanDescription = getPrimitiveBeanDescription(valueType);

        if (beanDescription == null) {
            //try json creator then
            beanDescription = getBeanDescriptionFromCreator(valueType);
        }

        if (beanDescription == null) {
            //if class does not have a json creator, get description from declaration
            beanDescription = getBeanDescriptionFromDeclaration(valueType);
        }

        return beanDescription;
    }

    /**
     * Get bean description as a primitive type, String is considered as a primitive type
     *
     * @param valueType value type
     * @return bean description
     */
    private <T> BeanDescription getPrimitiveBeanDescription(Class<T> valueType) {

        if (valueType == null) {
            return null;
        }

        if (valueType.isPrimitive() || valueType == String.class) {

            BeanDescription beanDescription = new BeanDescription();
            beanDescription.isPrimitive = true;
            Object value = getDefaultValue(valueType);
            beanDescription.addFieldDescription(null, valueType, value, null, null);
            return beanDescription;
        }
        return null;
    }

    /**
     * Get bean description from constructor
     *
     * @param valueType value type
     * @return bean description, may be NULL when class does not have a constructor annotated with JsonCreator
     */
    private <T> BeanDescription getBeanDescriptionFromCreator(Class<T> valueType) {

        //get constructor annotated with JsonCreator
//    	@SuppressWarnings("unchecked")
        Constructor<?>[] constructors = valueType.getConstructors();
        Constructor<?> constructor = null;
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(JsonCreator.class)) {
                constructor = c;
                break;
            }
        }

        if (constructor == null) {
            return null;
        }
        BeanDescription beanDescription = new BeanDescription();
        beanDescription.hasJsonCreator = true;
        beanDescription.constructor = constructor;
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Type[] genericTypes = constructor.getGenericParameterTypes();
        Annotation[][] annotations = constructor.getParameterAnnotations();

        BeanDescription.ConstructorParamDescription[] cpdArray = getDefaultConstructorDescription(constructor);

        //get field description from constructor parameters
        if (annotations != null) {
            int index = 0;
            for (Annotation[] fieldAnnotations : annotations) {
                if (fieldAnnotations != null) {
                    for (Annotation annotation : fieldAnnotations) {
                        if (annotation.annotationType() == JsonProperty.class) {
                            String key = ((JsonProperty) annotation).value();
                            Class<?> type = paramTypes[index];
                            BeanDescription.FieldDescription fd = new BeanDescription.FieldDescription(key, type, null, null, genericTypes[index]);
                            cpdArray[index].isJsonProperty = true;
                            cpdArray[index].fieldDescription = fd;        //Link constructor description with field description
                            beanDescription.addFieldDescription(fd);
                        }
                    }
                    index++;
                }
            }
        }

        beanDescription.addConstructorParamDescription(cpdArray);

        return beanDescription;
    }

    /**
     * Get bean description from declaration
     *
     * @param valueType value type
     * @return bean description, may be NULL when class does not have any field annotated with JsonProperty
     */
    private <T> BeanDescription getBeanDescriptionFromDeclaration(Class<T> valueType) {

        if (valueType == null) {
            return null;
        }

        //get filed and super class filed
        HashMap<String, Field> filedMap = new HashMap<String, Field>();
        for (Class<?> c = valueType; c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                if (!filedMap.containsKey(field.getName())) {
                    filedMap.put(field.getName(), field);
                }
            }
        }
        Field[] fields = new Field[filedMap.size()];
        filedMap.values().toArray(fields);

//    	Field[] fields = valueType.getDeclaredFields();
        if (fields == null) {
            return null;
        }

        BeanDescription beanDescription = null;
        for (Field field : fields) {
            if (field != null) {
                if (field.isAnnotationPresent(JsonProperty.class)) {
                    if (beanDescription == null) {
                        beanDescription = new BeanDescription();
                    }
                    JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                    String key = jsonProperty.value();
                    Class<?> type = field.getType();
                    String name = field.getName();
                    BeanDescription.FieldDescription fd = new BeanDescription.FieldDescription(key, type, null, name, field.getGenericType());
                    fd.field = field;
                    beanDescription.addFieldDescription(fd);
                }
            }
        }

        if (beanDescription != null) {

            //get constructor, prefer none parameter constructor
//    		@SuppressWarnings("unchecked")
            Constructor<?>[] constructors = valueType.getConstructors();
            for (Constructor<?> c : constructors) {
                Class<?>[] paramTypes = c.getParameterTypes();
                beanDescription.constructor = c;
                if (paramTypes == null || paramTypes.length == 0) {
                    break;
                }
            }

            BeanDescription.ConstructorParamDescription[] cpdArray = getDefaultConstructorDescription(beanDescription.constructor);
            beanDescription.addConstructorParamDescription(cpdArray);
        }

        return beanDescription;
    }

    /**
     * Fill bean description with value present in json String
     *
     * @param beanDescription bean description to fill
     * @param jsonObject      data source
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processMapping(BeanDescription beanDescription, JSONObject jsonObject) {

        if (beanDescription == null || beanDescription.fieldDescriptions == null) {
            return;
        }

        for (int i = 0; i < beanDescription.fieldDescriptions.size(); i++) {
            BeanDescription.FieldDescription fieldDescription = beanDescription.fieldDescriptions.get(i);
            String key = fieldDescription.key;
            Class<?> type = fieldDescription.type;
            Object value = fieldDescription.value;
            if (type.isArray()) {
                //Array
                JSONArray array = null;
                try {
                    array = jsonObject.getJSONArray(key);
                } catch (JSONException e) {
                }
                if (array != null) {
                    int length = array.length();
                    Class<?> nestType = type.getComponentType();
                    value = Array.newInstance(nestType, length);
                    for (int j = 0; j < length; j++) {
                        try {
                            BeanDescription nestBean = getBeanDescription(nestType);
                            Object nestInstance = null;
                            if (nestBean != null) {
                                if (nestBean.isPrimitive) {
                                    nestInstance = processPrimitiveArrayItem(array, j, nestBean);
                                } else {
                                    JSONObject obj = array.getJSONObject(j);
                                    processMapping(nestBean, obj);
                                    nestInstance = fillInstance(nestBean);
                                }
                            }
                            Array.set(value, j, nestInstance);
                        } catch (JSONException e) {
                            throw new RuntimeException("JSONArray Process error... ");
                        }
                    }
                }

            } else {

                boolean isArrayList = type.isAssignableFrom(List.class);
                if (isArrayList) {
                    //List
                    JSONArray array = null;
                    try {
                        array = jsonObject.getJSONArray(key);
                    } catch (JSONException e) {
                    }
                    if (array != null) {
                        int length = array.length();
                        Class<?> nestType = getListGenType(fieldDescription.genericType);
                        value = new ArrayList();
                        for (int j = 0; j < length; j++) {
                            try {
                                BeanDescription nestBean = getBeanDescription(nestType);
                                Object nestInstance = null;
                                if (nestBean != null) {
                                    if (nestBean.isPrimitive) {
                                        nestInstance = processPrimitiveArrayItem(array, j, nestBean);
                                    } else {
                                        JSONObject obj = array.getJSONObject(j);
                                        processMapping(nestBean, obj);
                                        nestInstance = fillInstance(nestBean);
                                    }
                                }
                                ((ArrayList) value).add(nestInstance);
                            } catch (JSONException e) {
                                throw new RuntimeException("JSONArray Process error... ");
                            }
                        }
                    }
                } else {
                    LOGD("isArrayList :" + isArrayList);

                    //Primitive
                    if (type == long.class) {
                        LOGD("fill field type long");
                        value = jsonObject.optLong(key);
                    } else if (type == int.class) {
                        LOGD("fill field type int");
                        value = jsonObject.optInt(key);
                    } else if (type == boolean.class) {
                        LOGD("fill field type boolean");
                        value = jsonObject.optBoolean(key);
                    } else if (type == float.class) {
                        value = (float) jsonObject.optDouble(key);
                    } else if (type == double.class) {
                        LOGD("fill field type double");
                        value = jsonObject.optDouble(key);
                    } else if (type == String.class) {
                        LOGD("fill field type String");
                        try {
                            value = jsonObject.getString(key);
                        } catch (Exception e) {
                        }
                    } else {
                        LOGD("other type : " + type);
                        BeanDescription nestBean = getBeanDescription(type);
                        Object nestInstance = null;
                        if (nestBean != null) {
                            JSONObject obj = jsonObject.optJSONObject(key);
                            if (obj != null) {
                                processMapping(nestBean, obj);
                                nestInstance = fillInstance(nestBean);
                                value = nestInstance;
                            }
                        }
                    }
                }
            }
            beanDescription.fieldDescriptions.get(i).value = value;

        }

    }

    /**
     * Process primitive array item
     *
     * @param array    Json array
     * @param index    current index
     * @param nestBean bean description
     * @return instance
     */
    private Object processPrimitiveArrayItem(JSONArray array, int index, BeanDescription nestBean) {

        if (nestBean.fieldDescriptions != null && nestBean.fieldDescriptions.size() == 1) {
            BeanDescription.FieldDescription fd = nestBean.fieldDescriptions.get(0);
            Class<?> type = fd.type;
            Object value = fd.value;
            if (type == long.class) {
                value = array.optLong(index);
            } else if (type == int.class) {
                value = array.optInt(index);
            } else if (type == boolean.class) {
                value = array.optBoolean(index);
            } else if (type == float.class) {
                value = (float) array.optDouble(index);
            } else if (type == double.class) {
                value = array.optDouble(index);
            } else if (type == String.class) {
                try {
                    value = array.getString(index);
                } catch (JSONException e) {
                }
            } else {
                LOGD("unknown type : " + type);
            }
            return value;
        }

        return null;
    }

    /**
     * Fill Java Bean with bean description
     *
     * @param beanDescription bean description that used to create java bean
     * @return filled instance
     */
    private Object fillInstance(BeanDescription beanDescription) {

        Constructor<?> constructor = beanDescription.constructor;
        ArrayList<BeanDescription.ConstructorParamDescription> cpds = beanDescription.constructorParamDescriptions;
        boolean useJsonCreator = beanDescription.hasJsonCreator;
        Object instance = null;

        if (constructor != null) {

            //create instance
            if (cpds != null) {
                Object[] params = new Object[cpds.size()];
                int index = 0;
                for (BeanDescription.ConstructorParamDescription cpd : cpds) {

                    //if isJsonProperty, use json creator to create instance
                    if (cpd.isJsonProperty) {
                        params[index] = cpd.fieldDescription.value;
                    } else {
                        params[index] = cpd.value;
                    }
                    index++;
                }
                try {
                    instance = constructor.newInstance(params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    //none parameter constructor
                    instance = constructor.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!useJsonCreator) {
                //no jsonCreator, use field description to fill instance
                ArrayList<BeanDescription.FieldDescription> fds = beanDescription.fieldDescriptions;
                if (fds != null) {
                    for (BeanDescription.FieldDescription fd : fds) {
                        fillProperty(fd, instance);
                    }
                }
            }

        }

        return instance;
    }

    /**
     * Fill instance's properties
     *
     * @param fieldDescription field description
     * @param instance         instance
     */
    private void fillProperty(BeanDescription.FieldDescription fieldDescription, Object instance) {

        if (fieldDescription == null || instance == null) {
            return;
        }

        //try direct set first, may throw exception when there is a security manager
        try {
            fieldDescription.field.setAccessible(true);
            fieldDescription.field.set(instance, fieldDescription.value);
            return;
        } catch (Exception e) {
        }

        //use setter
        Class<?> type = fieldDescription.type;
        String fieldName = fieldDescription.name;
        String setterName = null;
        if (!TextUtils.isEmpty(fieldName)) {
            setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
        try {
            Method setterMethod = instance.getClass().getMethod(setterName, type);

            if (setterMethod != null) {
                setterMethod.invoke(instance, fieldDescription.value);
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    /**
     * Get parameterized type in list
     *
     * @param genericType generic type
     * @return parameterized type in list
     */
    private Class<?> getListGenType(Type genericType) {

        if (genericType != null && genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] atas = parameterizedType.getActualTypeArguments();
            if (atas != null && atas.length > 0) {
                if (atas[0] instanceof Class) {
                    Class<?> nestType = (Class<?>) atas[0];
                    return nestType;
                }
            }
        }
        return null;
    }

    /**
     * Get default constructor description
     *
     * @param constructor constructor
     * @return constructor parameter descriptions
     */
    private BeanDescription.ConstructorParamDescription[] getDefaultConstructorDescription(Constructor<?> constructor) {

        BeanDescription.ConstructorParamDescription[] paramDescription = null;
        if (constructor != null) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes == null) {
                return null;
            }
            paramDescription = new BeanDescription.ConstructorParamDescription[paramTypes.length];
            int index = 0;
            for (Class<?> paramType : paramTypes) {
                Object param = getDefaultValue(paramType);
                BeanDescription.ConstructorParamDescription temp = new BeanDescription.ConstructorParamDescription(false, paramType, param);
                paramDescription[index++] = temp;
            }
        }
        return paramDescription;
    }

    /**
     * Get default value for parameter
     *
     * @param paramType parameter type
     * @return default value for the parameter
     */
    private Object getDefaultValue(Class<?> paramType) {
        Object param = null;
        if (paramType == int.class) {
            param = 0;
        } else if (paramType == long.class) {
            param = 0l;
        } else if (paramType == short.class) {
            param = 0;
        } else if (paramType == byte.class) {
            param = 0;
        } else if (paramType == double.class) {
            param = 0;
        } else if (paramType == float.class) {
            param = 0;
        } else if (paramType == char.class) {
            param = ' ';
        } else if (paramType == boolean.class) {
            param = false;
        } else {
            param = null;
        }
        return param;
    }

    private static final void LOGD(String content) {
//    	Log.v("c", "====" + content);
    }

}

/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.michael.corelib.internet.core;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.michael.corelib.config.CoreConfig;
import com.michael.corelib.internet.core.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * All Requests inherit from this MUST add Annotation (either
 * {@link RequiredParam} or {@link OptionalParam}) to their declared fields that
 * should be send to the REST server.
 * <p/>
 * Note : 1.Follow field should not be declared in Requests: api_key call_id sig
 * session_key format 2.REST version is set to "1.0" by default,
 *
 * @param <T>
 * @see RequiredParam
 * @see OptionalParam
 */
public abstract class RequestBase<T> {

    public static final class CustomHttpParams {

        public int connection_timeout = 20 * 1000;

        public int so_timeout = 20 * 1000;

        public int buffer_size = 8192;

        public boolean tcpNoDelay = true;

        public boolean staleCheckingEnabled = true;

    }

    private RequestEntity mRequestEntity;

    private CustomHttpParams mCustomHttpParams;

    private boolean mStringRawResponse;

    private boolean mShouldUrlEncodedParam;

    private boolean mShowResponseLog;

    public void setShowResponseLog(boolean show) {
        mShowResponseLog = show;
    }

    public boolean getShowResponseLog() {
        return mShowResponseLog;
    }

    public boolean isStringRawResponse() {
        return mStringRawResponse;
    }

    public boolean isShouldUrlEncodedParam() {
        return mShouldUrlEncodedParam;
    }

    final public void setCustomHttpParams(CustomHttpParams customHttpParams) {
        mCustomHttpParams = customHttpParams;
    }

    CustomHttpParams getCustomHttpParams() {
        return mCustomHttpParams;
    }

    public RequestEntity getRequestEntity() throws NetWorkException {
        if (mRequestEntity != null) {
            return mRequestEntity;
        }
        mRequestEntity = new RequestEntity();
        Bundle bundle = getHeaderParams();
        mRequestEntity.setBasicParams(getParams());
        mRequestEntity.setHeaderParams(bundle);
        mRequestEntity.setContentType(RequestEntity.REQUEST_CONTENT_TYPE_TEXT_PLAIN);
        if (bundle != null && bundle.containsKey(RequestEntity.HEADER_KEY_CONTENT_TYPE)) {
            mRequestEntity.setContentType(bundle.getString(RequestEntity.HEADER_KEY_CONTENT_TYPE));
        }
        return mRequestEntity;
    }

    protected String getMethodUrl() {
        Class<?> c = this.getClass();

        // Method name
        if (c.isAnnotationPresent(RestMethodUrl.class)) {
            RestMethodUrl restMethodName = c.getAnnotation(RestMethodUrl.class);
            return restMethodName.value();
        }

        return null;
    }

    protected Bundle getParams() throws NetWorkException {
        Class<?> c = this.getClass();
        ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
        while (c != RequestBase.class) {
            classList.add(0, c);
            c = c.getSuperclass();
        }

        if (CoreConfig.DEBUG) {
            for (Class<?> cl : classList) {
                LOGD("Class Name : " + cl.getName());
            }
        }

        Bundle ret = new Bundle();
        for (Class<?> cl : classList) {
            getParamsInternal(cl, ret);
        }

        if (!ret.containsKey("method")) {
            throw new RuntimeException("Method Name MUST be annotated!! :" + c.getName());
        }

        if (!ret.containsKey("httpMethod")) {
            throw new RuntimeException("Http Method Name Can not be annotated Empty!!");
        }

        return ret;
    }

    protected Bundle getHeaderParams() throws NetWorkException {
        Class<?> c = this.getClass();
        ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
        while (c != RequestBase.class) {
            classList.add(0, c);
            c = c.getSuperclass();
        }

        if (CoreConfig.DEBUG) {
            for (Class<?> cl : classList) {
                LOGD("Class Name : " + cl.getName());
            }
        }

        Bundle ret = new Bundle();
        for (Class<?> cl : classList) {
            getHeaderParamsInternal(cl, ret);
        }

        return ret;
    }

    private Bundle getHeaderParamsInternal(Class<?> c, Bundle bundle)  throws NetWorkException {
        Field[] fields = c.getDeclaredFields();
        Bundle params = bundle;

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                boolean hasDefault = false;
                long defaultValue = -1;

                if (field.isAnnotationPresent(HttpHeaderParam.class)) {
                    HttpHeaderParam headerParam = field.getAnnotation(HttpHeaderParam.class);
                    if (headerParam != null) {
                        String name = headerParam.value();
                        Object object = field.get(this);
                        if (object == null) {
                            throw new NetWorkException("Param " + name + " MUST NOT be null");
                        }
                        String value = String.valueOf(object);
                        if (TextUtils.isEmpty(value)) {
                            value = " ";
                        }
                        params.putString(name, value);
                    }
                }
            }  catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return params;
    }

    private Bundle getParamsInternal(Class<?> c, Bundle bundle) throws NetWorkException {
        Field[] fields = c.getDeclaredFields();
        Bundle params = bundle;

        // Method name or Base URL
        if (c.isAnnotationPresent(RestMethodUrl.class)) {
            RestMethodUrl restMethodName = c.getAnnotation(RestMethodUrl.class);
            String methodName = restMethodName.value();
            params.putString("method", methodName);
        }

        //ext url param
        if (c.isAnnotationPresent(RestMethodExtUrlParam.class)) {
            RestMethodExtUrlParam param = c.getAnnotation(RestMethodExtUrlParam.class);
            String paramString = param.value();
            params.putString("methodExt", paramString);
        }

        if (c.isAnnotationPresent(StringRawResponse.class)) {
            mStringRawResponse = true;
        }

        if (c.isAnnotationPresent(UrlEncodedParam.class)) {
            UrlEncodedParam data = c.getAnnotation(UrlEncodedParam.class);
            mShouldUrlEncodedParam = data.value();
        }

        // http Method name
        String httpMethod = "POST";
        if (c.isAnnotationPresent(HttpMethod.class)) {
            HttpMethod method = c.getAnnotation(HttpMethod.class);
            httpMethod = method.value();
        }
        if (!TextUtils.isEmpty(httpMethod)) {
            if (httpMethod.toUpperCase().equals("GET")) {
                params.putString("httpMethod", "GET");
            } else if (httpMethod.toUpperCase().equals("POST")) {
                params.putString("httpMethod", "POST");
            } else {
                throw new RuntimeException("Http Method Name Must be annotated POST or GET!!");
            }
        }

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                boolean hasDefault = false;
                long defaultValue = -1;
                if (field.isAnnotationPresent(IgnoreValue.class)) {
                    IgnoreValue value = field.getAnnotation(IgnoreValue.class);
                    if (value != null) {
                        hasDefault = true;
                        defaultValue = value.value();
                    }
                }

                if (field.isAnnotationPresent(RequiredParam.class)) {
                    RequiredParam requiredParam = field.getAnnotation(RequiredParam.class);
                    if (requiredParam != null) {
                        String name = requiredParam.value();
                        Object object = field.get(this);
                        if (object == null) {
                            throw new NetWorkException("Param " + name + " MUST NOT be null");
                        }
                        String value = String.valueOf(object);
                        if (TextUtils.isEmpty(value)) {
                            throw new NetWorkException("Param " + name + " MUST NOT be null");
                        }
                        params.putString(name, value);
                    }
                } else if (field.isAnnotationPresent(OptionalParam.class)) {
                    OptionalParam optionalParam = field.getAnnotation(OptionalParam.class);
                    if (optionalParam != null) {
                        String name = optionalParam.value();
                        Object object = field.get(this);
                        if (object != null) {
                            if (hasDefault) {
                                if (object instanceof Long) {
                                    long value = (Long) object;
                                    if (value != defaultValue) {
                                        params.putString(name, String.valueOf(value));
                                    }
                                } else if (object instanceof Integer) {
                                    int value = (Integer) object;
                                    if (value != defaultValue) {
                                        params.putString(name, String.valueOf(value));
                                    }
                                }
                            } else {
                                String value = String.valueOf(object);
                                params.putString(name, value);
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return params;
    }

    private void LOGD(String message) {
        if (CoreConfig.DEBUG) {
            Log.d(this.getClass().getName(), message);
        }
    }

    /**
     * 获取T的类型
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Class<T> getGenericType() {
        Type genType = getClass().getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return null;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (params.length < 1) {
            throw new RuntimeException("Index outof bounds");
        }
        if (!(params[0] instanceof Class)) {
            return null;
        }
        return (Class<T>) params[0];
    }
}

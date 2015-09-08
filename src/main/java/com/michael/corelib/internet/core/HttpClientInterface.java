package com.michael.corelib.internet.core;

import android.content.Context;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.util.List;

public interface HttpClientInterface {

    boolean init(Context context);

    <T> T getResource(Class<T> retResourceType, String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers, RequestBase.CustomHttpParams customHttpParams)
        throws NetWorkException;

    InputStream getInputStreamResource(String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers, RequestBase.CustomHttpParams customHttpParams)
        throws NetWorkException;

    <T> T postResource(Class<T> retResourceType, String requestUrl, String method, HttpEntity postEntity, List<NameValuePair> headers, RequestBase.CustomHttpParams customHttpParams)
        throws NetWorkException;

    boolean isNetworkAvailable();

}

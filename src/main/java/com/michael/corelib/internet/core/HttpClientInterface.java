package com.michael.corelib.internet.core;

import android.content.Context;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.util.List;

public interface HttpClientInterface {

    public void init(Context context);

    public <T> T getResource(Class<T> retResourceType, String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers)
        throws NetWorkException;

    public InputStream getInputStreamResource(String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers)
        throws NetWorkException;

    @Deprecated()
    public String getInputStreamResourceCallBackMode(String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers)
        throws NetWorkException;

    public <T> T postResource(Class<T> retResourceType, String requestUrl, String method, HttpEntity postEntity, List<NameValuePair> headers)
        throws NetWorkException;

    public boolean isNetworkAvailable();

    public void setHttpReturnListener(HttpRequestHookListener l);
}

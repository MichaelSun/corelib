package com.michael.corelib.internet.core;

import org.apache.http.client.methods.HttpRequestBase;

import java.io.InputStream;

/**
 * @author Guoqing Sun Nov 15, 20125:50:13 PM
 */
public interface HttpRequestHookListener {
    
    /**
     * 目前只有在大文件下载才会做此接口回调
     * 
     * @param request
     */
    void onCheckRequestHeaders(String requestUrl, HttpRequestBase request);
    
    String onInputStreamReturn(String requestUrl, InputStream is);
}

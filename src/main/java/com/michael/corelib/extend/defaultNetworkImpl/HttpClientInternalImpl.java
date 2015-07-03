package com.michael.corelib.extend.defaultNetworkImpl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import com.michael.corelib.internet.core.HttpClientInterface;
import com.michael.corelib.internet.core.HttpRequestHookListener;
import com.michael.corelib.internet.core.NetWorkException;
import com.michael.corelib.internet.core.util.InternetStringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by michael on 15/2/5.
 */
public class HttpClientInternalImpl implements HttpClientInterface {

    public static final String HTTP_REQUEST_METHOD_POST = "POST";

    public static final String HTTP_REQUEST_METHOD_GET = "GET";

    private static final int TIMEOUT_DELAY = 20 * 1000;

    private static final int HTTP_PORT = 80;

    private HttpClient httpClient;

    private HttpClient httpClientByte;

    private Context mContext;

    private HttpRequestHookListener mHttpRequestHookListener;

    public HttpClientInternalImpl() {
    }

    @Override
    public void init(Context context) {
        mContext = context;
        httpClient = createHttpClient();
        httpClientByte = createHttpClientByte();
    }

    @Override
    public <T> T getResource(Class<T> retResourceType, String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers) throws NetWorkException {
        HttpRequestBase requestBase = createHttpRequest(requestUrl, method, entity);

        // 增加指定的Header
        if (headers != null) {
            for (NameValuePair nvp : headers) {
                requestBase.addHeader(nvp.getName(), nvp.getValue());
            }
        }

        if (retResourceType == byte[].class) {
            try {
                byte[] ret = getBytesResponse(requestBase);
                return (T) ret;
            } catch (NetWorkException e) {
                e.printStackTrace();
            }
        } else if (retResourceType == String.class) {
            try {
                return (T) getStringResponse(requestBase);
            } catch (NetWorkException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Unknown resoureType :" + retResourceType);
        }

        return null;
    }

    @Override
    public InputStream getInputStreamResource(String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers) throws NetWorkException {
        HttpRequestBase requestBase = createHttpRequest(requestUrl, method, entity);

        // 增加指定的Header
        if (headers != null) {
            for (NameValuePair nvp : headers) {
                requestBase.addHeader(nvp.getName(), nvp.getValue());
            }
        }

        return getInputStreamResponse1(requestBase, requestUrl);
    }

    @Override
    public String getInputStreamResourceCallBackMode(String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers) throws NetWorkException {
        // 大文件下载，图片，语音等
        HttpRequestBase requestBase = createHttpRequest(requestUrl, method, entity);

        // 增加指定的Header
        if (headers != null) {
            for (NameValuePair nvp : headers) {
                requestBase.addHeader(nvp.getName(), nvp.getValue());
            }
        }

        if (mHttpRequestHookListener != null) {
            mHttpRequestHookListener.onCheckRequestHeaders(requestUrl, requestBase);
        }

        return getInputStreamResponse(requestBase, requestUrl);
    }

    @Override
    public <T> T postResource(Class<T> retResourceType, String requestUrl, String method, HttpEntity postEntity, List<NameValuePair> headers) throws NetWorkException {
        HttpRequestBase requestBase = createHttpRequest(requestUrl, method, postEntity);

        // 增加指定的Header
        if (headers != null) {
            for (NameValuePair nvp : headers) {
                requestBase.addHeader(nvp.getName(), nvp.getValue());
            }
        }

        if (retResourceType == byte[].class) {
            try {
                byte[] ret = getBytesResponse(requestBase);
                return (T) ret;
            } catch (NetWorkException e) {
                e.printStackTrace();
            }
        } else if (retResourceType == String.class) {
            try {
                return (T) getStringResponse(requestBase);
            } catch (NetWorkException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Unknown resoureType :" + retResourceType);
        }

        return null;
    }

    @Override
    public boolean isNetworkAvailable() {
        if (mContext == null) {
            return false;
        }

        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }

        NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setHttpReturnListener(HttpRequestHookListener l) {
        mHttpRequestHookListener = l;
    }


    private class StringResponseHandler implements ResponseHandler<String> {

        @Override
        public String handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {
            String r = null;
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    r = InternetStringUtils.unGzipBytesToString(response.getEntity().getContent()).trim();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return r;
        }
    }

    private class ByteDataResponseHandler implements ResponseHandler<byte[]> {

        @Override
        public byte[] handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {

            byte[] data = EntityUtils.toByteArray(response.getEntity());
            return data;
        }

    }

    private class StreamResponseHandler implements ResponseHandler<String> {

        private String mRequestUrl;

        StreamResponseHandler(String url) {
            mRequestUrl = url;
        }

        @Override
        public String handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {
            if (response != null
                    && response.getStatusLine() != null
                    && !String.valueOf(response.getStatusLine().getStatusCode()).startsWith("2")) {
                throw new IOException("exception happend when getting response. StatusCode=" + response.getStatusLine().getStatusCode());
            }

            InputStream is = response.getEntity().getContent();
            String ret = null;
            if (mHttpRequestHookListener != null) {
                ret = mHttpRequestHookListener.onInputStreamReturn(mRequestUrl, is);
            } else {
                throw new IOException("can't find HttpReturnInterface Impl");
            }

            return ret;
        }
    }

    private DefaultHttpClient createHttpClientByte() {
        final SchemeRegistry supportedSchemes = new SchemeRegistry();
        supportedSchemes.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), HTTP_PORT));
//		supportedSchemes.register(new Scheme("https", EasySSLSocketFactory
//				.getSocketFactory(), 443));
//        supportedSchemes.register(new Scheme("https", SSLSocketFactoryEx.getSocketFactory(), 443));
        final HttpParams httpParams = createHttpParams();
        final ThreadSafeClientConnManager tccm = new ThreadSafeClientConnManager(httpParams, supportedSchemes);
        DefaultHttpClient client = new DefaultHttpClient(tccm, httpParams);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        return client;
    }

    private DefaultHttpClient createHttpClient() {
        final SchemeRegistry supportedSchemes = new SchemeRegistry();
        supportedSchemes.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        supportedSchemes.register(new Scheme("https", SSLSocketFactoryEx.getSocketFactory(), 443));
        final HttpParams httpParams = createHttpParams();
        final ThreadSafeClientConnManager tccm = new ThreadSafeClientConnManager(httpParams, supportedSchemes);
        DefaultHttpClient client = new DefaultHttpClient(tccm, httpParams);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        return client;
    }

    private HttpParams createHttpParams() {
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_DELAY);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT_DELAY);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpClientParams.setRedirecting(params, false);
        ConnManagerParams.setMaxTotalConnections(params, 50);
        ConnManagerParams.setTimeout(params, TIMEOUT_DELAY);
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(20));
        return params;
    }

    private HttpRequestBase createHttpRequest(String url, String method, HttpEntity entity) {
        checkParams(url, method);
        HttpRequestBase httpRequest = null;
        if (method.equalsIgnoreCase(HTTP_REQUEST_METHOD_GET)) {
            httpRequest = new HttpGet(url);
        } else {
            httpRequest = new HttpPost(url);
            if (entity != null) {
                ((HttpPost) httpRequest).setEntity(entity);
            }
        }

        HttpHost host = HttpProxy.getProxyHttpHost(mContext);
        if (host != null) {
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
            httpClientByte.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
        } else {
            httpClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
        }
        return httpRequest;
    }

    private void checkParams(String url, String method)
        throws IllegalArgumentException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Request url MUST NOT be null");
        }
        if (TextUtils.isEmpty(method)) {
            throw new IllegalArgumentException(
                                                  "Request method MUST NOT be null");
        } else {
            if (!method.equalsIgnoreCase(HTTP_REQUEST_METHOD_GET)
                    && !method.equalsIgnoreCase(HTTP_REQUEST_METHOD_POST)) {
                throw new IllegalArgumentException("Only support GET and POST");
            }
        }
    }

    private void preExecuteHttpRequest() {
        httpClient.getConnectionManager().closeExpiredConnections();
    }

    private void onExecuteException(HttpRequestBase httpRequest) {
        httpRequest.abort();
    }

    private String getInputStreamResponse(HttpRequestBase httpRequest, String url) throws NetWorkException {
        try {
            preExecuteHttpRequest();
            StreamResponseHandler handler = new StreamResponseHandler(url);
            return httpClientByte.execute(httpRequest, handler);
        } catch (Exception e) {
            onExecuteException(httpRequest);
            throw new NetWorkException(NetWorkException.NETWORK_ERROR, "网络连接错误", e.toString());
        }
    }

    private InputStream getInputStreamResponse1(HttpRequestBase httpRequest, String url) throws NetWorkException {
        preExecuteHttpRequest();
        try {
            HttpResponse response = httpClientByte.execute(httpRequest);
            if (response != null
                    && response.getStatusLine() != null
                    && !String.valueOf(response.getStatusLine().getStatusCode()).startsWith("2")) {
                throw new IOException("exception happend when getting response. StatusCode=" + response.getStatusLine().getStatusCode());
            }

            if (response != null) {
                return response.getEntity().getContent();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private byte[] getBytesResponse(HttpRequestBase httpRequest)
        throws NetWorkException {
        try {
            preExecuteHttpRequest();
            ByteDataResponseHandler handler = new ByteDataResponseHandler();
            return httpClientByte.execute(httpRequest, handler);
        } catch (Exception e) {
            onExecuteException(httpRequest);
            throw new NetWorkException(NetWorkException.NETWORK_ERROR, "网络连接错误", e.toString());
        }
    }

    private String getStringResponse(HttpRequestBase httpRequest)
        throws NetWorkException {
        try {
            preExecuteHttpRequest();
            StringResponseHandler handler = new StringResponseHandler();
            return httpClient.execute(httpRequest, handler);
        } catch (Exception e) {
            onExecuteException(httpRequest);
            throw new NetWorkException(NetWorkException.NETWORK_ERROR, "网络连接错误", e.toString());
        }
    }
}

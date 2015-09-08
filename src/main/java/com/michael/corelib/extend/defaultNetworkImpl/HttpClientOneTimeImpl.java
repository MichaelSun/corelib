package com.michael.corelib.extend.defaultNetworkImpl;

import android.content.Context;
import android.text.TextUtils;
import com.michael.corelib.internet.core.NetWorkException;
import com.michael.corelib.internet.core.RequestBase;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Created by michael on 15/9/8.
 */
public class HttpClientOneTimeImpl {

    private static final String HTTP_REQUEST_METHOD_POST = "POST";

    private static final String HTTP_REQUEST_METHOD_GET = "GET";

    private static final int TIMEOUT_DELAY = 20 * 1000;

    private Context mContext;

    public HttpClientOneTimeImpl(Context context) {
        mContext = context;
    }

    public <T> T getResourceOneTime(Class<T> retResourceType, String requestUrl, String method,
                                    HttpEntity entity, List<NameValuePair> headers,
                                    RequestBase.CustomHttpParams customHttpParams) throws NetWorkException {
        if (retResourceType == byte[].class) {
            throw new RuntimeException("Not support this resource type");
        } else if (retResourceType == String.class) {
            throw new RuntimeException("Not support this resource type");
        } else if (retResourceType == HttpResponse.class) {
            try {
                HttpClient httpClient = createHttpClient(customHttpParams);
                if (httpClient == null) {
                    return null;
                }
                HttpRequestBase requestBase = createHttpRequest(httpClient, requestUrl, method, entity, headers);
                if (requestBase == null) {
                    return null;
                }

                return (T) httpClient.execute(requestBase);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Unknown resoureType :" + retResourceType);
        }

        return null;
    }

    public <T> T postResourceOneTime(Class<T> retResourceType, String requestUrl, String method,
                              HttpEntity postEntity, List<NameValuePair> headers, RequestBase.CustomHttpParams customHttpParams) throws NetWorkException {
        if (retResourceType == byte[].class) {
            throw new RuntimeException("Not support this resource type");
        } else if (retResourceType == String.class) {
            throw new RuntimeException("Not support this resource type");
        } else if (retResourceType == HttpResponse.class) {
            try {
                HttpClient httpClient = createHttpClient(customHttpParams);
                if (httpClient == null) {
                    return null;
                }
                HttpRequestBase requestBase = createHttpRequest(httpClient, requestUrl, method, postEntity, headers);
                if (requestBase == null) {
                    return null;
                }

                return (T) httpClient.execute(requestBase);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Unknown resoureType :" + retResourceType);
        }

        return null;
    }

    private DefaultHttpClient createHttpClient(RequestBase.CustomHttpParams customHttpParams) {
        try {
            HttpParams httpParams = createHttpParams(customHttpParams);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", sf, 443));
            ClientConnectionManager conManager = new ThreadSafeClientConnManager(httpParams, schReg);

            DefaultHttpClient client = new DefaultHttpClient(conManager, httpParams);
            client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
            return client;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return null;
    }

    private HttpParams createHttpParams(RequestBase.CustomHttpParams customHttpParams) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(params, customHttpParams.staleCheckingEnabled);
        HttpConnectionParams.setConnectionTimeout(params, customHttpParams.connection_timeout);
        HttpConnectionParams.setSoTimeout(params, customHttpParams.so_timeout);
        HttpConnectionParams.setSocketBufferSize(params, customHttpParams.buffer_size);
        HttpConnectionParams.setTcpNoDelay(params, customHttpParams.tcpNoDelay);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpClientParams.setRedirecting(params, false);
        return params;
    }


    private HttpRequestBase createHttpRequest(HttpClient httpClient, String url, String method, HttpEntity entity, List<NameValuePair> headers) {
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
        } else {
            httpClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
        }

        // 增加指定的Header
        if (headers != null) {
            for (NameValuePair nvp : headers) {
                httpRequest.addHeader(nvp.getName(), nvp.getValue());
            }
        }

        return httpRequest;
    }

    private void checkParams(String url, String method)
        throws IllegalArgumentException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Request url MUST NOT be null");
        }
        if (TextUtils.isEmpty(method)) {
            throw new IllegalArgumentException("Request method MUST NOT be null");
        } else {
            if (!method.equalsIgnoreCase(HTTP_REQUEST_METHOD_GET) && !method.equalsIgnoreCase(HTTP_REQUEST_METHOD_POST)) {
                throw new IllegalArgumentException("Only support GET and POST");
            }
        }
    }

}

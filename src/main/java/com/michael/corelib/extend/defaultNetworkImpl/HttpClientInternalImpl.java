package com.michael.corelib.extend.defaultNetworkImpl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import com.michael.corelib.config.CoreConfig;
import com.michael.corelib.internet.core.HttpClientInterface;
import com.michael.corelib.internet.core.NetWorkException;
import com.michael.corelib.internet.core.RequestBase;
import com.michael.corelib.internet.core.util.InternetStringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
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
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Created by michael on 15/2/5.
 */
public class HttpClientInternalImpl implements HttpClientInterface {

    private static final String HTTP_REQUEST_METHOD_POST = "POST";

    private static final String HTTP_REQUEST_METHOD_GET = "GET";

    private static final int TIMEOUT_DELAY = 20 * 1000;

    private HttpClient mHttpClient;

    private HttpClient mHttpClientByte;

    private Context mContext;

    public HttpClientInternalImpl() {
    }

    @Override
    public boolean init(Context context) {
        mContext = context;
        try {
            mHttpClient = createHttpClient();
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
        try {
            mHttpClientByte = createHttpClientByte();
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

        if (mHttpClient == null || mHttpClientByte == null) {
            return false;
        }

        return true;
    }

    @Override
    public <T> T getResource(Class<T> retResourceType, String requestUrl, String method,
                             HttpEntity entity, List<NameValuePair> headers, RequestBase.CustomHttpParams customHttpParams) throws NetWorkException {
        if (customHttpParams != null) {
            CoreConfig.LOGD("[[HttpClientInternalImpl::getResource]] customHttpParams make the http client use one time");
            return new HttpClientOneTimeImpl(mContext).getResourceOneTime(retResourceType, requestUrl, method, entity, headers, customHttpParams);
        }

        HttpRequestBase requestBase = createHttpRequest(mHttpClient, mHttpClientByte, requestUrl, method, entity);
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
        } else if (retResourceType == HttpResponse.class) {
            preExecuteHttpRequest();
            try {
                return (T) mHttpClient.execute(requestBase);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Unknown resoureType :" + retResourceType);
        }

        return null;
    }

    @Override
    public InputStream getInputStreamResource(String requestUrl, String method,
                                              HttpEntity entity, List<NameValuePair> headers, RequestBase.CustomHttpParams customHttpParams) throws NetWorkException {
        if (customHttpParams != null) {
            CoreConfig.LOGD("[[HttpClientInternalImpl::getInputStreamResource]] customHttpParams make the http client use one time");
            throw new RuntimeException("Not support this Now");
        }

        HttpRequestBase requestBase = createHttpRequest(mHttpClient, mHttpClientByte, requestUrl, method, entity);
        // 增加指定的Header
        if (headers != null) {
            for (NameValuePair nvp : headers) {
                requestBase.addHeader(nvp.getName(), nvp.getValue());
            }
        }

        return getInputStreamResponse1(requestBase, requestUrl);
    }

    @Override
    public <T> T postResource(Class<T> retResourceType, String requestUrl, String method,
                              HttpEntity postEntity, List<NameValuePair> headers, RequestBase.CustomHttpParams customHttpParams) throws NetWorkException {
        if (customHttpParams != null) {
            CoreConfig.LOGD("[[HttpClientInternalImpl::getResource]] customHttpParams make the http client use one time");
            return new HttpClientOneTimeImpl(mContext).postResourceOneTime(retResourceType, requestUrl, method, postEntity, headers, customHttpParams);
        }

        HttpRequestBase requestBase = createHttpRequest(mHttpClient, mHttpClientByte, requestUrl, method, postEntity);
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
        } else if (retResourceType == HttpResponse.class) {
            preExecuteHttpRequest();
            try {
                return (T) mHttpClient.execute(requestBase);
            } catch (IOException e) {
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

    private class StringResponseHandler implements ResponseHandler<String> {

        @Override
        public String handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {
            String r = null;
            if (response != null
                    && response.getStatusLine() != null
                    && String.valueOf(response.getStatusLine().getStatusCode()).startsWith("2")) {
                try {
                    String str = InternetStringUtils.unGzipBytesToString(response.getEntity().getContent());
                    if(!TextUtils.isEmpty(str)){
                        r = str.trim();
                    }
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

    private DefaultHttpClient createHttpClientByte() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        HttpParams httpParams = createHttpParams();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", sf, 443));
        ThreadSafeClientConnManager tccm = new ThreadSafeClientConnManager(httpParams, schReg);

        DefaultHttpClient client = new DefaultHttpClient(tccm, httpParams);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        return client;
    }

    private DefaultHttpClient createHttpClient() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        HttpParams httpParams = createHttpParams();
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

    private HttpRequestBase createHttpRequest(HttpClient httpClient, HttpClient httpClientByte, String url, String method, HttpEntity entity) {
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
            throw new IllegalArgumentException("Request method MUST NOT be null");
        } else {
            if (!method.equalsIgnoreCase(HTTP_REQUEST_METHOD_GET) && !method.equalsIgnoreCase(HTTP_REQUEST_METHOD_POST)) {
                throw new IllegalArgumentException("Only support GET and POST");
            }
        }
    }

    private void preExecuteHttpRequest() {
        mHttpClient.getConnectionManager().closeExpiredConnections();
    }

    private void onExecuteException(HttpRequestBase httpRequest) {
        httpRequest.abort();
    }

    private InputStream getInputStreamResponse1(HttpRequestBase httpRequest, String url) throws NetWorkException {
        preExecuteHttpRequest();
        try {
            HttpResponse response = mHttpClientByte.execute(httpRequest);
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
            return mHttpClientByte.execute(httpRequest, handler);
        } catch (Exception e) {
            onExecuteException(httpRequest);
            throw new NetWorkException(NetWorkException.NETWORK_ERROR, "Network connect error", e.toString(), null);
        }
    }

    private String getStringResponse(HttpRequestBase httpRequest)
        throws NetWorkException {
        try {
            preExecuteHttpRequest();
            StringResponseHandler handler = new StringResponseHandler();
            return mHttpClient.execute(httpRequest, handler);
        } catch (Exception e) {
            onExecuteException(httpRequest);
            throw new NetWorkException(NetWorkException.NETWORK_ERROR, "Network connect error", e.toString(), null);
        }
    }
}

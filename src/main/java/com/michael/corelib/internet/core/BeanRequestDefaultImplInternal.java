package com.michael.corelib.internet.core;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.michael.corelib.extend.defaultNetworkImpl.MultipartHttpEntity;
import com.michael.corelib.internet.NetworkLog;
import com.michael.corelib.internet.core.util.InternetStringUtils;
import com.michael.corelib.internet.core.util.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

class BeanRequestDefaultImplInternal implements BeanRequestInterface {

    private static final boolean DEBUG = NetworkLog.DEBUG;

    private static final String KEY_METHOD = "method";

    private static final String KEY_HTTP_METHOD = "httpMethod";

    private static final String KEY_METHOD_EXT = "methodExt";

    private static BeanRequestDefaultImplInternal mInstance;

    private HttpClientInterface mHttpClientInterface;

    private Context mContext;

    private static Object lockObject = new Object();

    public static BeanRequestDefaultImplInternal getInstance(Context context) {
        if (mInstance == null) {
            synchronized (lockObject) {
                if (mInstance == null) {
                    mInstance = new BeanRequestDefaultImplInternal(context);
                }
            }
        }
        return mInstance;
    }

    private BeanRequestDefaultImplInternal(Context context) {
        mHttpClientInterface = HttpClientFactory.createHttpClientInterface(context);
        mContext = context;
    }

    @Override
    public <T> T request(RequestBase<T> request) throws NetWorkException {
        long entryTime = System.currentTimeMillis();
        if (DEBUG) {
            NetworkLog.LOGD("Entery Internet request, current time = " + entryTime + "ms from 1970");
        }

        if (request == null) {
            throw new NetWorkException(NetWorkException.REQUEST_NULL, "Request can't be NUll", null, null);
        }

        if (!mHttpClientInterface.isNetworkAvailable()) {
            throw new NetWorkException(NetWorkException.NETWORK_NOT_AVILABLE, "NetWork error, not wifi or mobile", null, null);
        }

        RequestEntity requestEntity = request.getRequestEntity();
        Bundle baseParams = requestEntity.getBasicParams();
        Bundle headerBundle = requestEntity.getHeaderParams();

        if (baseParams == null) {
            throw new NetWorkException(NetWorkException.PARAM_EMPTY, "Params can't be Null", null, null);
        }

        //make Http scheme url
        String api_url = baseParams.getString(KEY_METHOD);
        baseParams.remove(KEY_METHOD);
        if (baseParams.containsKey(KEY_METHOD_EXT)) {
            String ext = baseParams.getString(KEY_METHOD_EXT);
            api_url = api_url + "/" + ext;
            baseParams.remove(KEY_METHOD_EXT);
        }

        String httpMethod = baseParams.getString(KEY_HTTP_METHOD);
        baseParams.remove(KEY_HTTP_METHOD);
        String contentType = requestEntity.getContentType();
        if (contentType == null) {
            throw new NetWorkException(NetWorkException.MISS_CONTENT, "Content type must be specified", null, null);
        }

        if (DEBUG) {
            StringBuilder param = new StringBuilder();
            if (baseParams != null) {
                for (String key : baseParams.keySet()) {
                    param.append("|    ").append(key).append(" : ").append(baseParams.get(key)).append("\n");
                }

                param.append("|    <<<<<<<<<< header params >>>>>>>>>>").append("\n");
                for (String key : headerBundle.keySet()) {
                    param.append("|    ").append(key).append(" : ").append(headerBundle.get(key)).append("\n");
                }
            }

            NetworkLog.LOGD("\n\n//*****\n| [[request::" + request + "]] \n" + "| RestAPI URL = " + api_url
                    + "\n| Params is = \n" + param + " \n\\\\*****\n");
        }

        HttpEntity entity = null;
        if (!contentType.equals(RequestEntity.REQUEST_CONTENT_TYPE_MUTIPART)) {
            if (httpMethod.equals("POST")) {
                List<NameValuePair> paramList = convertBundleToNVPair(baseParams);
                if (paramList != null) {
                    try {
                        Collections.sort(paramList, new Comparator<NameValuePair>() {
                            @Override
                            public int compare(NameValuePair p1, NameValuePair p2) {
                                return p1.getName().compareTo(p2.getName());
                            }
                        });

                        StringBuilder paramSb = new StringBuilder();
                        for (NameValuePair pair : paramList) {
                            paramSb.append(pair.getName()).append("=").append(pair.getValue()).append("&");
                        }
                        String data = null;
                        if (paramSb.length() > 0) {
                            data = paramSb.subSequence(0, paramSb.length() - 1).toString();
                        }

//                        if (DEBUG) {
//                            NetworkLog.LOGD("[[request]] POST data : " + data);
//                        }

                        if (!TextUtils.isEmpty(data)) {
                            entity = new StringEntity(data);
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new NetWorkException(NetWorkException.ENCODE_HTTP_PARAMS_ERROR, "Unable to encode http parameters", null, null);
                    }
                }
            } else if (httpMethod.equals("GET")) {
                StringBuilder sb = new StringBuilder(api_url);
                sb.append("?");
                for (String key : baseParams.keySet()) {
                    sb.append(key).append("=").append(baseParams.getString(key)).append("&");
                }
                api_url = sb.substring(0, sb.length() - 1);
//                if (DEBUG) {
//                    NetworkLog.LOGD("\n\n//***\n| GET url : " + api_url + "\n\\\\***\n");
//                }
            }
        } else if (contentType.equals(RequestEntity.REQUEST_CONTENT_TYPE_MUTIPART)) {
            requestEntity.setBasicParams(baseParams);
            entity = new MultipartHttpEntity(requestEntity);
        }

        HttpResponse httpResponse = null;
        if ("POST".equals(httpMethod)) {
            httpResponse = mHttpClientInterface.postResource(HttpResponse.class, api_url, httpMethod, entity, convertBundleToNVPair(headerBundle));
        } else if ("GET".equals(httpMethod)) {
            httpResponse = mHttpClientInterface.getResource(HttpResponse.class, api_url, httpMethod, entity, convertBundleToNVPair(headerBundle));
        } else {
            throw new IllegalArgumentException("Lib not support this http method : " + httpMethod);
        }

        if (httpResponse == null) {
            throw new NetWorkException(NetWorkException.SERVER_ERROR, "Server response is null", null, null);
        }

        String response = null;
        if (httpResponse.getStatusLine() != null
                && String.valueOf(httpResponse.getStatusLine().getStatusCode()).startsWith("2")) {
            try {
                response = InternetStringUtils.unGzipBytesToString(httpResponse.getEntity().getContent()).trim();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dumpResponse(request.getClass().getName(), response);

        if (response == null) {
            NetworkResponse networkResponse = new NetworkResponse(httpResponse.getStatusLine().getStatusCode(), null, httpResponse.getAllHeaders());
            throw new NetWorkException(httpResponse.getStatusLine().getStatusCode(), "Connect to server error", null, networkResponse);
        }

        T ret = null;
        try {
            if (request.isStringRawResponse() || (request.getGenericType() == String.class)) {
                return (T) response;
            } else {
                ret = JsonUtils.parse(response, request.getGenericType());

                if (ret != null && ret instanceof ResponseBase) {
                    NetworkResponse info = new NetworkResponse(httpResponse.getStatusLine().getStatusCode(), response, httpResponse.getAllHeaders());
                    ((ResponseBase) ret).networkResponse = info;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    public String getSig(Bundle params, String secret_key) {
        if (params == null || params.size() == 0) {
            return null;
        }

        TreeMap<String, String> sortParams = new TreeMap<String, String>();
        for (String key : params.keySet()) {
            sortParams.put(key, params.getString(key));
        }

        Vector<String> vecSig = new Vector<String>();
        for (String key : sortParams.keySet()) {
            String value = sortParams.get(key);
            if (value.length() > NetworkLog.SIG_PARAM_MAX_LENGTH) {
                value = value.substring(0, NetworkLog.SIG_PARAM_MAX_LENGTH);
            }
            vecSig.add(key + "=" + value);
        }
        // LOGD("[[getSig]] after operate, the params is : " + vecSig);

        String[] nameValuePairs = new String[vecSig.size()];
        vecSig.toArray(nameValuePairs);

        for (int i = 0; i < nameValuePairs.length; i++) {
            for (int j = nameValuePairs.length - 1; j > i; j--) {
                if (nameValuePairs[j].compareTo(nameValuePairs[j - 1]) < 0) {
                    String temp = nameValuePairs[j];
                    nameValuePairs[j] = nameValuePairs[j - 1];
                    nameValuePairs[j - 1] = temp;
                }
            }
        }
        StringBuffer nameValueStringBuffer = new StringBuffer();
        for (int i = 0; i < nameValuePairs.length; i++) {
            nameValueStringBuffer.append(nameValuePairs[i]);
        }

        nameValueStringBuffer.append(secret_key);
        String sig = InternetStringUtils.MD5Encode(nameValueStringBuffer.toString());
        return sig;
    }

    private List<NameValuePair> convertBundleToNVPair(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            list.add(new BasicNameValuePair(key, bundle.getString(key)));
        }

        return list;
    }

    private void dumpResponse(String request, String response) {
        if (DEBUG) {
            NetworkLog.LOGD(response);
            long endTime = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder(1024);
            sb.append("\n\n")
                .append("//*****\n")
                .append("| ------------- begin response ------------\n")
                .append("|\n")
                .append("| [[Request::" + request + "]] raw response string = ");
            NetworkLog.LOGD(sb.toString());
            sb.setLength(0);
            if (response != null) {
                try {
                    sb.append("| " + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sb.append("| null");
            }
            int step = 1024;
            int index = 0;
            do {
                if (index >= sb.length()) {
                    break;
                } else {
                    if ((index + step) < sb.length()) {
                        NetworkLog.LOGD(sb.substring(index, index + step));
                    } else {
                        NetworkLog.LOGD(sb.substring(index, sb.length()));
                    }
                }
                index = index + step;
            } while (index < sb.length());
            sb.setLength(0);
            sb.append("|\n").append("| ------------- end response ------------\n").append("\\\\*****\n");
            NetworkLog.LOGD(sb.toString());
        }
    }

}

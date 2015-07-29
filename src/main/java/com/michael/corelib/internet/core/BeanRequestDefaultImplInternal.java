package com.michael.corelib.internet.core;

import android.content.Context;
import android.os.Bundle;
import com.michael.corelib.extend.defaultNetworkImpl.MultipartHttpEntity;
import com.michael.corelib.internet.NetworkLog;
import com.michael.corelib.internet.core.util.InternetStringUtils;
import com.michael.corelib.internet.core.util.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

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
            throw new NetWorkException(NetWorkException.REQUEST_NULL, "Request can't be NUll", null);
        }

        if (!mHttpClientInterface.isNetworkAvailable()) {
            throw new NetWorkException(NetWorkException.NETWORK_NOT_AVILABLE, "网络连接错误，请检查您的网络", null);
        }

        RequestEntity requestEntity = request.getRequestEntity();
        Bundle baseParams = requestEntity.getBasicParams();
        Bundle headerBundle = requestEntity.getHeaderParams();

        if (baseParams == null) {
            throw new NetWorkException(NetWorkException.PARAM_EMPTY, "网络请求参数列表不能为空", null);
        }

        String api_url = baseParams.getString(KEY_METHOD);
        baseParams.remove(KEY_METHOD);
        String httpMethod = baseParams.getString(KEY_HTTP_METHOD);
        baseParams.remove(KEY_HTTP_METHOD);
        if (baseParams.containsKey(KEY_METHOD_EXT)) {
            String ext = baseParams.getString(KEY_METHOD_EXT);
            api_url = api_url + "/" + ext;
            baseParams.remove(KEY_METHOD_EXT);
        }

        String contentType = requestEntity.getContentType();
        if (contentType == null) {
            throw new NetWorkException(NetWorkException.MISS_CONTENT, "Content Type MUST be specified", null);
        }

        if (DEBUG) {
            StringBuilder param = new StringBuilder();
            if (baseParams != null) {
                for (String key : baseParams.keySet()) {
                    param.append("|    ").append(key).append(" : ").append(baseParams.get(key)).append("\n");
                }

                param.append("|    ======= header params ========").append("\n").append("|    ");
                for (String key : headerBundle.keySet()) {
                    param.append("|    ").append(key).append(" : ").append(headerBundle.get(key)).append("\n");
                }
            }

            NetworkLog.LOGD("\n\n//***\n| [[request::" + request + "]] \n" + "| RestAPI URL = " + api_url
                    + "\n| after getSig bundle params is = \n" + param + " \n\\\\***\n");
        }

        HttpEntity entity = null;
        if (!contentType.equals(RequestEntity.REQUEST_CONTENT_TYPE_MUTIPART)) {
            if (httpMethod.equals("POST")) {
                List<NameValuePair> paramList = convertBundleToNVPair(baseParams);
                if (paramList != null) {
                    try {
                        entity = new UrlEncodedFormEntity(paramList, HTTP.UTF_8);
                    } catch (UnsupportedEncodingException e) {
                        throw new NetWorkException(NetWorkException.ENCODE_HTTP_PARAMS_ERROR, "Unable to encode http parameters", null);
                    }
                }
            } else if (httpMethod.equals("GET")) {
                StringBuilder sb = new StringBuilder(api_url);
                sb.append("?");
                for (String key : baseParams.keySet()) {
                    sb.append(key).append("=").append(baseParams.getString(key)).append("&");
                }
                api_url = sb.substring(0, sb.length() - 1);
                if (DEBUG) {
                    NetworkLog.LOGD("\n\n//***\n| GET url : " + api_url + "\n\\\\***\n");
                }
            }
        } else if (contentType.equals(RequestEntity.REQUEST_CONTENT_TYPE_MUTIPART)) {
            requestEntity.setBasicParams(baseParams);
            entity = new MultipartHttpEntity(requestEntity);
        }

        String response = null;
        if ("POST".equals(httpMethod)) {
            response = mHttpClientInterface.postResource(String.class, api_url, httpMethod, entity, convertBundleToNVPair(headerBundle));
        } else if ("GET".equals(httpMethod)) {
            response = mHttpClientInterface.getResource(String.class, api_url, httpMethod, entity, convertBundleToNVPair(headerBundle));
        } else {
            throw new IllegalArgumentException("Lib Not Support this HTTP Method : " + httpMethod);
        }

        dumpResponse(request.getClass().getName(), response);

        if (response == null) {
            throw new NetWorkException(NetWorkException.SERVER_ERROR, "服务器错误，请稍后重试", null);
        }

        T ret = null;
        try {
            if (request.isStringRawResponse() || (request.getGenericType() == String.class)) {
                return (T) response;
            } else {
//                boolean isJsonObject = true;
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                } catch (JSONException e) {
//                    isJsonObject = false;
//                    e.printStackTrace();
//                }
//                if (!isJsonObject) {
//                    Log.e("BeanRequestInternal", "return Json data is JsonArray without key");
//                    response = "{\"data\":" + response + "}";
//                    Log.e("BeanRequestInternal", "NOW JSON RET = " + response);
//                }

                ret = JsonUtils.parse(response, request.getGenericType());

                if (ret != null && ret instanceof ResponseBase) {
                    ((ResponseBase) ret).originJsonString = response;
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
                .append("//***\n")
                .append("| ------------- begin response ------------\n")
                .append("|\n")
                .append("| [[request::" + request + "]] raw response String = ");
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
            sb.append("|\n").append("| ------------- end response ------------\n").append("\\\\***");
            NetworkLog.LOGD(sb.toString());
        }
    }

}

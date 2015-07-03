package com.michael.corelib.extend.OKHttpNetworkImpl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.michael.corelib.internet.core.HttpClientInterface;
import com.michael.corelib.internet.core.HttpRequestHookListener;
import com.michael.corelib.internet.core.NetWorkException;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.util.List;

/**
 * Created by michael on 15/2/3.
 */
public class OKHttpClientImpl implements HttpClientInterface {

    private static final String HTTP_REQUEST_METHOD_POST = "POST";

    private static final String HTTP_REQUEST_METHOD_GET = "GET";

    private static OKHttpClientImpl instance;

    private static Object lockObject = new Object();

    private HttpRequestHookListener mHttpReturnInterface;

    private Context mContext;

    public OKHttpClientImpl() {
    }

    public static OKHttpClientImpl getInstance(Context context) {
        if (instance == null) {
            synchronized (lockObject) {
                if (instance == null) {
                    instance = new OKHttpClientImpl();
                }
            }
        }
        return instance;
    }


    @Override
    public void setHttpReturnListener(HttpRequestHookListener l) {
        mHttpReturnInterface = l;
    }


    @Override
    public void init(Context context) {
        mContext = context;
    }

    @Override
    public <T> T getResource(Class<T> resourceType, String url, String method, HttpEntity entity, List<NameValuePair> headers) throws NetWorkException {
//        Request.Builder builder = new Request.Builder();
//        if (headers != null) {
//            for (NameValuePair pair : headers) {
//                builder.header(pair.getName(), pair.getValue());
//            }
//        }
//
//        Request request = null;
//        if (HTTP_REQUEST_METHOD_GET.equals(method)) {
//            request = builder.url(url).build();
//        } else if (HTTP_REQUEST_METHOD_POST.equals(method)) {
//            if (entity instanceof MultipartHttpEntity) {
//                Bundle params = ((MultipartHttpEntity) entity).getRequestEntity().getBasicParams();
//                ArrayList<RequestEntity.MultipartFileItem> fileItems = ((MultipartHttpEntity) entity).getRequestEntity().getFileItems();
//                MultipartBuilder multiBuilder = new MultipartBuilder("-------------1234567890123456789").type(MultipartBuilder.FORM);
//                if (params != null) {
//                    for (String key : params.keySet()) {
//                        multiBuilder.addFormDataPart(key, params.getString(key));
//                    }
//                }
//                if (fileItems != null) {
//                    MultipartBuilder fileBuilder = new MultipartBuilder("-----------------9876543210");
//                    for (RequestEntity.MultipartFileItem item : fileItems) {
//                        fileBuilder.addPart(Headers.of("Content-Disposition", "myFile; filename=\"" + item.getFileName() + "\""),
//                                               RequestBody.create(MediaType.parse(item.getContentType()), item.getFile()));
//                    }
//                    multiBuilder.addFormDataPart("files", null, fileBuilder.build());
//                }
//                request = builder.url(url).post(multiBuilder.build()).build();
//            } else {
//                MediaType contentType = MediaType.parse("text/plain");
//                try {
//                    RequestBody body = RequestBody.create(contentType, InputStreamUtils.InputStreamTOString(entity.getContent()));
//                    request = builder.url(url).post(body).build();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//        try {
//            Response response = okHttpClient.newCall(request).execute();
//            if (resourceType == byte[].class) {
//                return (T) response.body().bytes();
//            } else if (resourceType == String.class) {
//                return (T) response.body().string();
//            }  else {
//                throw new RuntimeException("Unknown resoureType :" + resourceType);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return null;
    }

    @Override
    public InputStream getInputStreamResource(String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers) throws NetWorkException {
        return null;
    }

    @Override
    public String getInputStreamResourceCallBackMode(String requestUrl, String method, HttpEntity entity, List<NameValuePair> headers) throws NetWorkException {
        return null;
    }

    @Override
    public <T> T postResource(Class<T> retResourceType, String requestUrl, String method, HttpEntity postEntity, List<NameValuePair> headers) throws NetWorkException {
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

}

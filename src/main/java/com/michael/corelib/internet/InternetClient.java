package com.michael.corelib.internet;

import android.content.Context;
import com.michael.corelib.coreutils.CustomThreadPool;
import com.michael.corelib.internet.core.NetWorkException;
import com.michael.corelib.internet.core.NetworkResponse;
import com.michael.corelib.internet.core.RequestBase;
import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.util.List;

/**
 * Created by michael on 15/1/8.
 */
public class InternetClient {

    public interface NetworkCallback<T> {

        void onSuccess(RequestBase<T> request, T ret);

        void onFailed(RequestBase<T> request, NetworkResponse httpResponseCode);
    }

    private Context mContext;

    private static InternetClient gInternetClient;

    public static InternetClient getInstance(Context context) {
        if (gInternetClient == null) {
            synchronized (InternetClient.class) {
                if (gInternetClient == null) {
                    gInternetClient = new InternetClient(context);
                }
            }
        }

        return gInternetClient;
    }

    private InternetClient(Context context) {
        mContext = context;
    }

    public <T> void postRequest(final RequestBase<T> request, final NetworkCallback<T> callback) {
        CustomThreadPool.asyncWork(new Runnable() {
            @Override
            public void run() {
                NetworkResponse networkResponse = null;
                try {
                    T ret = InternetUtilInternal.request(mContext, request);
                    if (ret != null && callback != null) {
                        callback.onSuccess(request, ret);
                    }
                    return;
                } catch (NetWorkException e) {
                    if (NetworkLog.DEBUG) {
                        NetworkLog.LOGD(e.toString());
                    }
                    networkResponse = e.networkResponse;
                    e.printStackTrace();
                }

                if (callback != null) {
                    callback.onFailed(request, networkResponse);
                }
            }
        });
    }

    public <T> T syncPostRequest(RequestBase<T> request) throws NetWorkException {
        return InternetUtilInternal.request(mContext, request);
    }

    public InputStream downloadFile(String downloadUrl, List<NameValuePair> headers) throws NetWorkException {
        return InternetUtilInternal.requestInputstreamResource(mContext, downloadUrl, headers);
    }

}

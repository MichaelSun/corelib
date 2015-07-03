package com.michael.corelib.internet;

import android.content.Context;
import com.michael.corelib.internet.core.*;
import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.util.List;

class InternetUtilInternal {

	/**
	 * 同步接口 发送REST请求
	 *
	 * @param <T>
	 * @param request
	 *            REST请求
	 * @return REST返回
	 */
    @Deprecated
	public static <T> T request(Context context, RequestBase<T> request) throws NetWorkException {
		if (context != null && BeanRequestFactory.createBeanRequestInterface(context.getApplicationContext()) != null) {
			return BeanRequestFactory.createBeanRequestInterface(context.getApplicationContext()).request(request);
		}

		return null;
	}


	/**
	 * 大文件下载接口
	 *
	 * @param context
	 * @param imageUrl
	 * @return
	 */
//	public static String requestBigResourceWithCache(Context context, String imageUrl, List<NameValuePair> headers) throws NetWorkException {
//		if (context != null && HttpClientFactory.createHttpClientInterface(context.getApplicationContext()) != null) {
//            return HttpClientFactory.createHttpClientInterface(context.getApplicationContext()).getInputStreamResourceCallBackMode(
//                     imageUrl, "GET", null, headers);
//		}
//
//		return null;
//	}

    public static InputStream requestInputstreamResource(Context context, String url, List<NameValuePair> headers) throws NetWorkException {
        if (context != null && HttpClientFactory.createHttpClientInterface(context.getApplicationContext()) != null) {
            return HttpClientFactory.createHttpClientInterface(context.getApplicationContext()).getInputStreamResource(url, "GET", null, headers);
        }

        return null;
    }

//    @Deprecated
//	public static void setHttpBeanRequestImpl(BeanRequestInterface impl) {
//		BeanRequestFactory.setgBeanRequestInterfaceImpl(impl);
//	}

    @Deprecated
	public static void setHttpReturnListener(Context context, HttpRequestHookListener l) {
		if (context != null && HttpClientFactory.createHttpClientInterface(context.getApplicationContext()) != null) {
			HttpClientFactory.createHttpClientInterface(context.getApplicationContext()).setHttpReturnListener(l);
		}
	}

}

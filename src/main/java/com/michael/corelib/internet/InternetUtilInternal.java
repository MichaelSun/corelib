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

    public static InputStream requestInputstreamResource(Context context, String url, List<NameValuePair> headers) throws NetWorkException {
        if (context != null && HttpClientFactory.createHttpClientInterface(context.getApplicationContext()) != null) {
            return HttpClientFactory.createHttpClientInterface(context.getApplicationContext()).getInputStreamResource(url, "GET", null, headers, null);
        }

        return null;
    }

}

package com.michael.corelib.filedownload;

import android.text.TextUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by michael on 15/1/7.
 */
public class DownloadRequest {

    /**
     * 下载回调接口
     */
    public interface DownloadListener {

        public static final int DOWNLOAD_SUCCESS = 10001;
        public static final int DOWNLOAD_FAILED = 20001;
        public static final int DOWNLOAD_CANCELED = 20002;

        void onDownloadProcess(int fileSize, int downloadSize);

        void onDownloadFinished(int status, Object response);
    }


    public static final int STATUS_NORMAL = 1000;
    public static final int STATUS_CANCEL = 1001;

    protected String mDownloadUrl;
    protected int mUrlHashCode;
    protected int mStatus;
    protected String mFileExtension; // 下载文件的扩展名
    protected List<NameValuePair> mHeaders = new LinkedList<NameValuePair>(); // 下载请求需要包含的头部

    protected AtomicBoolean requestIsOperating = new AtomicBoolean(false);

    protected DownloadListener mDownloadListener;

    public DownloadRequest(String downloadUrl, DownloadListener l) {
        this(downloadUrl, null, l);
    }

    public DownloadRequest(String downloadUrl, String fileExtension, DownloadListener l) {
        if (TextUtils.isEmpty(downloadUrl)) {
            throw new IllegalArgumentException("download url can't be empty");
        }

        mDownloadUrl = downloadUrl;
        mFileExtension = fileExtension;
        mStatus = STATUS_NORMAL;
        mUrlHashCode = mDownloadUrl.hashCode();
        mDownloadListener = l;
    }

    public List<NameValuePair> getHeaders() {
        return mHeaders;
    }

    private void addHeader(String name, String value) {
        mHeaders.add(new BasicNameValuePair(name, value));
    }

    public static class Builder {
        DownloadRequest mRequest;

        public Builder(String downloadUrl) {
            mRequest = new DownloadRequest(downloadUrl, null);
            mRequest.mStatus = STATUS_NORMAL;
            mRequest.mUrlHashCode = downloadUrl == null ? 0 : downloadUrl.hashCode();
        }

        public DownloadRequest create() {
            return mRequest;
        }

        public Builder setExtension(String ext) {
            mRequest.mFileExtension = ext;
            return this;
        }

        public Builder addHeader(String name, String value) {
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                mRequest.addHeader(name, value);
            }
            return this;
        }

        public Builder setDownloadListener(DownloadListener l) {
            mRequest.mDownloadListener = l;
            return this;
        }
    }

    public void cancelDownload() {
        mStatus = STATUS_CANCEL;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public int getUrlHashCode() {
        return mUrlHashCode;
    }

    public void setDownloadListener(DownloadListener mDownloadListener) {
        this.mDownloadListener = mDownloadListener;
    }

    public DownloadListener getDownloadListener() {
        return mDownloadListener;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getFileExtension() {
        return mFileExtension;
    }

    public AtomicBoolean getRequestIsOperating() {
        return requestIsOperating;
    }

    @Override
    public String toString() {
        return "DownloadRequest{" +
                   "mDownloadUrl='" + mDownloadUrl + '\'' +
                   ", mUrlHashCode=" + mUrlHashCode +
                   ", mStatus=" + mStatus +
                   ", mFileExtension='" + mFileExtension + '\'' +
                   ", mHeaders=" + mHeaders +
                   ", requestIsOperating=" + requestIsOperating +
                   '}';
    }
}

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
    public static final int STATUS_NORMAL = 1000;
    public static final int STATUS_CANCEL = 1001;

    public enum DOWNLOAD_TYPE {
        RAW, IMAGE
    }

    protected String mDownloadUrl;
    protected int mUrlHashCode;
    protected DOWNLOAD_TYPE mType;
    protected int mStatus;
    protected String mFileExtension; // 下载文件的扩展名
    protected List<NameValuePair> mHeaders = new LinkedList<NameValuePair>(); // 下载请求需要包含的头部

    protected AtomicBoolean requestIsOperating = new AtomicBoolean(false);

    public DownloadRequest(String downloadUrl) {
        this(DOWNLOAD_TYPE.RAW, downloadUrl);
    }

    public DownloadRequest(String downloadUrl, String extension) {
        this(DOWNLOAD_TYPE.RAW, downloadUrl, extension);
    }

    public DownloadRequest(DOWNLOAD_TYPE type, String downloadUrl) {
        if (TextUtils.isEmpty(downloadUrl)) {
            throw new IllegalArgumentException("download url can't be empty");
        }

        mDownloadUrl = downloadUrl;
        mType = type;
        mStatus = STATUS_NORMAL;
        mUrlHashCode = mDownloadUrl.hashCode();
    }

    public DownloadRequest(DOWNLOAD_TYPE type, String downloadUrl,
                           String fileExtension) {
        this(type, downloadUrl);
        mFileExtension = fileExtension;
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
            mRequest = new DownloadRequest(downloadUrl);
            mRequest.mStatus = STATUS_NORMAL;
            mRequest.mUrlHashCode = downloadUrl == null ? 0 : downloadUrl
                                                                  .hashCode();
        }

        public DownloadRequest create() {
            return mRequest;
        }

        public Builder setDownloadType(DOWNLOAD_TYPE type) {
            mRequest.mType = type;
            return this;
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
    }

    public void cancelDownload() {
        mStatus = STATUS_CANCEL;
    }

    public String getmDownloadUrl() {
        return mDownloadUrl;
    }

    public int getmUrlHashCode() {
        return mUrlHashCode;
    }

    public DOWNLOAD_TYPE getmType() {
        return mType;
    }

    public int getmStatus() {
        return mStatus;
    }

    public AtomicBoolean getRequestIsOperating() {
        return requestIsOperating;
    }

    @Override
    public String toString() {
        return "DownloadRequest [mDownloadUrl=" + mDownloadUrl
                   + ", mUrlHashCode=" + mUrlHashCode + ", mType=" + mType
                   + ", mStatus=" + mStatus + ", requestIsOperating="
                   + requestIsOperating + "]";
    }

}

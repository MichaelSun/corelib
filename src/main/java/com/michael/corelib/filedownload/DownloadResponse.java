package com.michael.corelib.filedownload;

/**
* Created by michael on 15/1/7.
*/
public class DownloadResponse {
    /**
     * 下载的URL
     */
    protected String mDownloadUrl;

    /**
     * 下载的图片的本地存储路径，如果文件下载成功，那么此路劲指向的就是真正的本地图片储存路径，如果文件下载 失败，或是没有下载完成，那么为空。
     */
    protected String mLocalRawPath;

    /**
     * 下载请求的Request对象
     */
    protected DownloadRequest mRequest;

    DownloadResponse() {
    }

    DownloadResponse(String downloadUrl, String rawPath, DownloadRequest request) {
        mDownloadUrl = downloadUrl;
        mLocalRawPath = rawPath;
        mRequest = request;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public String getRawLocalPath() {
        return mLocalRawPath;
    }

    public DownloadRequest getRequest() {
        return mRequest;
    }

    @Override
    public String toString() {
        return "DownloadResponse{" +
                   "mDownloadUrl='" + mDownloadUrl + '\'' +
                   ", mLocalRawPath='" + mLocalRawPath + '\'' +
                   ", mRequest=" + mRequest +
                   '}';
    }
}

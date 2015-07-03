package com.michael.corelib.filedownload;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import com.michael.corelib.config.CoreConfig;
import com.michael.corelib.coreutils.*;
import com.michael.corelib.fileutils.FileUtil;
import com.michael.corelib.internet.InternetClient;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileDownloader extends SingleInstanceBase implements Runnable, Destroyable {

    private static final String TAG = FileDownloader.class.getSimpleName();

    protected static final boolean DEBUG = CoreConfig.DEBUG;

    protected static final boolean SUPPORT_RANGED = true;

    protected static final boolean RUNTIME_CLOSE_SUPPORTED = false;

    protected String INPUT_STREAM_CACHE_PATH = null;
    protected String DOWNLOADED_FILE_DIR = null;

    // 默认为后进先下载
    protected boolean mLastInFirstDownload = true;

    /**
     * 当下载一个文件的时候，通过一个URL生成下载文件的本地的文件名字
     *
     * @author michael
     */
    public static interface DownloadFilenameCreateListener {
        /**
         * 为一个下载URL生成本地的文件路径，注意：要保证生成文件名字的唯一性 生成的文件会下载到 big_file_cache 文件夹下面
         *
         * @param downloadUrl
         * @return
         */
        String onFilenameCreateWithDownloadUrl(String downloadUrl);
    }

    private final class DefaultDownloadUrlEncodeListener implements DownloadFilenameCreateListener {

        @Override
        public String onFilenameCreateWithDownloadUrl(String downloadUrl) {
            int pos = downloadUrl.lastIndexOf(".");
            int sliptor = downloadUrl.lastIndexOf(File.separator);
            if (pos != -1 && sliptor != -1 && pos > sliptor) {
                String prefix = downloadUrl.substring(0, pos);
                return StringUtils.stringHashCode((prefix.replace(":", "+").replace("/", "_").replace(".", "-") + downloadUrl.substring(pos)));
            }
            return StringUtils.stringHashCode(downloadUrl.replace(":", "+").replace("/", "_").replace(".", "-"));
        }

    }

    protected DownloadFilenameCreateListener mDefaultDownloadFilenameCreateListener = new DefaultDownloadUrlEncodeListener();
    protected DownloadFilenameCreateListener mDownloadFilenameCreateListener = mDefaultDownloadFilenameCreateListener;

    public static final int DOWNLOAD_SUCCESS = 10001;
    public static final int DOWNLOAD_FAILED = 20001;

    public static interface DownloadListener {

        void onDownloadProcess(int fileSize, int downloadSize);

        void onDownloadFinished(int status, Object response);
    }

    protected static class DownloadListenerObj {

        public final DownloadListener mDownloadListener;

        public final String mFileUrl;

        public final int code;

        DownloadListenerObj(String url, DownloadListener listener) {
            mDownloadListener = listener;
            mFileUrl = url;
            code = mFileUrl.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            DownloadListenerObj downloadObj = (DownloadListenerObj) obj;
            if (downloadObj.code == code
                    && downloadObj.mDownloadListener == mDownloadListener) {
                return true;
            }

            return false;
        }
    }

    protected List<DownloadListenerObj> mListenerList;

    public static interface WorkListener {
        void onProcessWork(Runnable r);
    }

    public static final int NOTIFY_DOWNLOAD_SUCCESS = -20000;
    public static final int NOTIFY_DOWNLOAD_FAILED = -40000;

    protected static final int DEFAULT_KEEPALIVE = 5 * 1000;

    protected final NotifyHandlerObserver mSuccessHandler = new NotifyHandlerObserver(NOTIFY_DOWNLOAD_SUCCESS);
    protected final NotifyHandlerObserver mFailedHandler = new NotifyHandlerObserver(NOTIFY_DOWNLOAD_FAILED);
    protected Object objLock = new Object();
    protected boolean bIsStop = true;

    protected boolean bIsWaiting = false;
    protected ArrayList<DownloadRequest> mRequestList;
    protected Context mContext;
    protected long mKeepAlive;

    private WorkListener mWorkListener = new WorkListener() {
        @Override
        public void onProcessWork(Runnable r) {
            if (r != null) {
                CustomThreadPool.getInstance().excuteWithSpecialThread(FileDownloader.class.getSimpleName(), new CustomThreadPool.TaskWrapper(r));
            }
        }
    };

    public static FileDownloader getInstance(Context context) {
        return SingleInstanceBase.getInstance(FileDownloader.class);
    }

    /**
     * 设置后进先下载开关(默认为后提交先下载)
     */
    public void setLastInFirstDownloadEnabled(boolean enabled) {
        this.mLastInFirstDownload = enabled;
    }

    /**
     * 设置下载文件的存储路径
     *
     * @param dirFullPath
     */
    public void setDownloadDir(String dirFullPath) {
        if (!TextUtils.isEmpty(dirFullPath)) {
            File dirFile = new File(dirFullPath);
            if (dirFile.exists() && dirFile.isFile()) {
                dirFile.delete();
            }

            boolean mkSuccess = false;
            if (!dirFile.exists()) {
                mkSuccess = dirFile.mkdirs();
            } else {
                mkSuccess = true;
            }

            if (mkSuccess) {
                DOWNLOADED_FILE_DIR = dirFullPath;
                INPUT_STREAM_CACHE_PATH = dirFullPath + "/stream_cache/";
                return;
            }
        }

        throw new IllegalArgumentException("Can't make dir : " + dirFullPath);
    }

    public String getDownloadDir() {
        return DOWNLOADED_FILE_DIR;
    }

    protected FileDownloader() {
        super();
    }

    private void processWorks() {
        mWorkListener.onProcessWork(this);
    }

    @Deprecated
    public void registeSuccessHandler(Handler handler) {
        mSuccessHandler.registeObserver(handler);
    }

    @Deprecated
    public void registeFailedHandler(Handler handler) {
        mFailedHandler.registeObserver(handler);
    }

    @Deprecated
    public void unRegisteSuccessHandler(Handler handler) {
        mSuccessHandler.unRegisteObserver(handler);
    }

    @Deprecated
    public void unRegisteFailedHandler(Handler handler) {
        mFailedHandler.unRegisteObserver(handler);
    }

    public DownloadFilenameCreateListener setDownloadUrlEncodeListener(
                                                                          DownloadFilenameCreateListener l) {
        DownloadFilenameCreateListener ret = mDownloadFilenameCreateListener;
        mDownloadFilenameCreateListener = l;

        return ret;
    }

    public synchronized Boolean isStopped() {
        return bIsStop;
    }

    // 检查缓存目录中是否已经下载过文件
    private String checkFromCache(DownloadRequest request) {
        if (request != null && !TextUtils.isEmpty(request.mDownloadUrl)) {
            String saveUrl = mDownloadFilenameCreateListener != null ? mDownloadFilenameCreateListener
                                                                           .onFilenameCreateWithDownloadUrl(request.mDownloadUrl)
                                 : mDefaultDownloadFilenameCreateListener
                                       .onFilenameCreateWithDownloadUrl(request.mDownloadUrl);
            if (TextUtils.isEmpty(DOWNLOADED_FILE_DIR)) {
                return null;
            }
            File dir = new File(DOWNLOADED_FILE_DIR);
            if (!dir.exists() || dir.isFile()) {
                return null;
            }
            String extension = TextUtils.isEmpty(request.mFileExtension) ? ""
                                   : "." + request.mFileExtension;
            File cachedFile = new File(DOWNLOADED_FILE_DIR + saveUrl
                                           + extension);
            if (cachedFile.exists()) {

                if (DEBUG) {
                    CoreConfig.LOGD("<<<<< [[find in cache]] >>>>> ::::::::: " + cachedFile.getAbsolutePath());
                }
                return cachedFile.getAbsolutePath();
            }
        }
        if (DEBUG) {
            CoreConfig.LOGD("<<<<< [[can not find in cache]] >>>>> ::::::::: " + request.toString());
        }
        return null;
    }

    public boolean postRequest(DownloadRequest request, DownloadListener l) {
        if (mRequestList == null || request == null
                || TextUtils.isEmpty(request.mDownloadUrl) || l == null) {
            return false;
        }

        DownloadListenerObj downloadObj = new DownloadListenerObj(request.mDownloadUrl, l);
        boolean contain = false;
        synchronized (mListenerList) {
            for (DownloadListenerObj obj : mListenerList) {
                if (downloadObj.equals(obj)) {
                    contain = true;
                }
            }
            if (!contain) {
                mListenerList.add(downloadObj);
            }
        }

        // 检查是否已经下载过此request对应的文件
        String cachedFile = checkFromCache(request);
        if (!TextUtils.isEmpty(cachedFile)) {
            File file = new File(cachedFile);
            if (file.exists()) {
                DownloadResponse response = tryToHandleDownloadFile(cachedFile,
                                                                       request);
                if (response != null) {
                    mSuccessHandler.notifyAll(-1, -1, response);
                    handleProcess(request.mDownloadUrl, (int) file.length(),
                                     (int) file.length());
                    if (l != null) {
                        handleResponseByListener(DOWNLOAD_SUCCESS,
                                                    request.mDownloadUrl, response, false);
                    }
                }
                return true;
            }
        }

        return postRequest(request);
    }

    /**
     * 新提交的request会默认
     *
     * @param request
     * @return
     */
    public boolean postRequest(DownloadRequest request) {
        if (mRequestList == null || request == null || TextUtils.isEmpty(request.mDownloadUrl)) {
            return false;
        }

        if (DEBUG) {
            CoreConfig.LOGD("<<<<< [[postRequest]] >>>>> ::::::::: " + request.toString());
        }

        // 检查是否已经下载过此request对应的文件
        String cachedFile = checkFromCache(request);
        if (!TextUtils.isEmpty(cachedFile)) {
            File file = new File(cachedFile);
            if (file.exists()) {
                DownloadResponse response = tryToHandleDownloadFile(cachedFile,
                                                                       request);
                if (response != null) {
                    mSuccessHandler.notifyAll(-1, -1, response);
                    handleProcess(request.mDownloadUrl, (int) file.length(),
                                     (int) file.length());
                }
                return true;
            }
        }

        synchronized (mRequestList) {
            boolean contain = false;
            for (DownloadRequest r : mRequestList) {
                if (r.mUrlHashCode == request.mUrlHashCode) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                // mRequestList.add(request);
                // 将最新添加的任务放在下载队列的最前面
                if (mLastInFirstDownload) {
                    mRequestList.add(0, request);
                } else {
                    mRequestList.add(request);
                }

                if (DEBUG) {
                    CoreConfig.LOGD("postRequest, add request : " + request.toString() + " into download list");
                }
            }
            bIsStop = false;

            CustomThreadPool.ThreadPoolSnapShot tss = CustomThreadPool.getInstance()
                                                          .getSpecialThreadSnapShot(FileDownloader.class.getSimpleName());
            if (tss == null) {
                return false;
            } else {
                if (tss.taskCount < tss.ALLOWED_MAX_TAKS) {
                    if (DEBUG) {
                        CoreConfig.LOGD("entry into [[postRequest]] to start process ");
                    }
                    processWorks();
                }
            }
        }
        if (DEBUG) {
            CoreConfig.LOGD("<<<<< [[postRequest]]  end synchronized (mRequestList) >>>>>");
        }

        synchronized (objLock) {
            if (bIsWaiting) {
                bIsWaiting = false;

                if (DEBUG) {
                    CoreConfig.LOGD("try to notify download process begin");
                }
                objLock.notify();
            }
        }

        if (DEBUG) {
            CoreConfig.LOGD("<<<<< [[postRequest]]  end synchronized (objLock) >>>>>");
        }

        return true;
    }

    protected boolean checkInputStreamDownloadFile(String filePath) {
        return true;
    }

    protected DownloadResponse tryToHandleDownloadFile(
                                                          String downloadLocalPath, DownloadRequest request) {
        DownloadResponse response = new DownloadResponse();
        response.mDownloadUrl = request.mDownloadUrl;
        response.mLocalRawPath = downloadLocalPath;
        response.mRequest = request;

        return response;
    }

    private void waitforUrl() {
        try {
            synchronized (objLock) {
                if (mRequestList.size() == 0 && !bIsWaiting) {
                    bIsWaiting = true;

                    if (DEBUG) {
                        CoreConfig.LOGD("entry into [[waitforUrl]] for " + DEFAULT_KEEPALIVE + "ms");
                    }
                    objLock.wait(mKeepAlive);

                    if (DEBUG) {
                        CoreConfig.LOGD("leave [[waitforUrl]] for " + DEFAULT_KEEPALIVE + "ms");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (DEBUG) {
                CoreConfig.LOGD("Excption : ", e);
            }
        }
        bIsWaiting = false;
    }

    public List<NameValuePair> onCheckRequestHeaders(String requestUrl, List<NameValuePair> headers) {
        if (headers == null) {
            throw new IllegalArgumentException("Http Request is null");
        }

        if (SUPPORT_RANGED) {
            // 目前只有大文件下载才会做此接口回调，在此回调中可以增加断点续传
            String saveFile = mDownloadFilenameCreateListener != null
                                  ? mDownloadFilenameCreateListener.onFilenameCreateWithDownloadUrl(requestUrl)
                                  : mDefaultDownloadFilenameCreateListener.onFilenameCreateWithDownloadUrl(requestUrl);
            File bigCacheFile = new File(INPUT_STREAM_CACHE_PATH);
            if (!bigCacheFile.exists() || !bigCacheFile.isDirectory()) {
                bigCacheFile.delete();
                bigCacheFile.mkdirs();
            }

            File tempFile = new File(INPUT_STREAM_CACHE_PATH + saveFile);
            long fileSize = 0;
            if (tempFile.exists()) {
                fileSize = tempFile.length();
            } else {
                fileSize = 0;
            }

            headers.add(new BasicNameValuePair("RANGE", "bytes=" + fileSize + "-"));
        }

        return headers;
    }

    public String onInputStreamReturn(String requestUrl, InputStream is) {
        // if (!UtilsRuntime.isSDCardReady()) {
        // UtilsConfig.LOGD("return because unmount the sdcard");
        // return null;
        // }

        if (DEBUG) {
            CoreConfig.LOGD("");
            CoreConfig.LOGD("//-------------------------------------------------");
            CoreConfig.LOGD("||");
            CoreConfig.LOGD("|| [[FileDownloader::onInputStreamReturn]] : ");
            CoreConfig.LOGD("||      try to download [[BIG]] file with url : "
                                + requestUrl);
            CoreConfig.LOGD("||");
            CoreConfig.LOGD("\\-------------------------------------------------");
            CoreConfig.LOGD("");
        }

        if (is != null) {
            String saveUrl = mDownloadFilenameCreateListener != null
                                 ? mDownloadFilenameCreateListener.onFilenameCreateWithDownloadUrl(requestUrl)
                                 : mDefaultDownloadFilenameCreateListener.onFilenameCreateWithDownloadUrl(requestUrl);
            File bigCacheFile = new File(INPUT_STREAM_CACHE_PATH);
            if (!bigCacheFile.exists() || !bigCacheFile.isDirectory()) {
                bigCacheFile.delete();
                bigCacheFile.mkdirs();
            }

            long curTime = 0;
            if (DEBUG) {
                CoreConfig.LOGD("try to download from inputstream to local path = "
                                    + INPUT_STREAM_CACHE_PATH
                                    + saveUrl
                                    + " for orgin URL : " + requestUrl);
                curTime = System.currentTimeMillis();
            }

            // download file
            int totalSize = 0;
            try {
                totalSize = is.available();
            } catch (Exception e) {
                e.printStackTrace();
            }

            long downloadSize = 0;
            String savePath = null;
            String targetPath = INPUT_STREAM_CACHE_PATH + saveUrl;
            byte[] buffer = new byte[4096 * 2];
            File f = new File(targetPath);
            int len;
            OutputStream os = null;
            boolean isClosed = false;
            try {
                if (f.exists()) {
                    downloadSize = f.length();
                }

                os = new FileOutputStream(f, true);
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);

                    // add listener to Notify UI
                    downloadSize += len;
                    handleProcess(requestUrl, totalSize, (int) downloadSize);

                    if (RUNTIME_CLOSE_SUPPORTED) {
                        DownloadRequest r = findCacelRequest(requestUrl);
                        if (r != null
                                && r.mStatus == DownloadRequest.STATUS_CANCEL) {
                            CoreConfig.LOGD("try to close is >>>>>>>>>>>>>>>>>>>>");
                            is.close();
                            isClosed = true;
                        }
                    }
                }
                savePath = targetPath;
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                buffer = null;
            }
            // end download

            try {
                if (!isClosed) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!isClosed && !TextUtils.isEmpty(savePath)
                    && checkInputStreamDownloadFile(savePath)) {
                if (DEBUG) {
                    long successTime = System.currentTimeMillis();
                    CoreConfig.LOGD("[[onInputStreamReturn]] save Request url : "
                                        + saveUrl
                                        + " success ||||||| and the saved file size : "
                                        + FileUtil
                                              .convertStorage(new File(savePath)
                                                                  .length())
                                        + ", save cost time = "
                                        + (successTime - curTime) + "ms");
                }

                return savePath;
            } else {
                // 遗留文件，用于下次的断点下载
                if (DEBUG) {
                    CoreConfig.LOGD("===== failed to downlaod requestUrl : "
                                        + requestUrl + " beacuse the debug 断点 =====");
                }
                return null;
            }
        } else {
            if (DEBUG) {
                CoreConfig.LOGD("===== failed to downlaod requestUrl : "
                                    + requestUrl + " beacuse requestUrl is NULL =====");
            }
        }

        return null;
    }

    @Override
    public void onDestroy() {
        mSuccessHandler.removeAllObserver();
        mFailedHandler.removeAllObserver();
        synchronized (mRequestList) {
            mRequestList.clear();
        }

        synchronized (mListenerList) {
            mListenerList.clear();
        }
    }

    @Override
    public void run() {
        while (!bIsStop) {
            waitforUrl();

            if (DEBUG) {
                CoreConfig.LOGD("<<<<< [[run]] >>>>>");
            }
            synchronized (mRequestList) {
                if (mRequestList.size() == 0) {
                    // bIsRunning = false;
                    bIsStop = true;
                    break;
                }
            }

            if (DEBUG) {
                CoreConfig.LOGD("<<<<< [[run]]  end synchronized (mRequestList) >>>>>");
            }

            DownloadRequest request = null;
            try {
                request = findRequestCanOperate(mRequestList);
                if (request == null) {
                    bIsStop = true;
                }
                if (request != null
                        && request.mStatus != DownloadRequest.STATUS_CANCEL) {
                    if (DEBUG) {
                        CoreConfig.LOGD("================ <<" + Thread.currentThread().getName() + ">> working on : ");
                        CoreConfig.LOGD("begin operate one request : " + request.toString());
                        CoreConfig.LOGD("============================================");
                    }

//                    String cacheFile = InternetUtilInternal.requestBigResourceWithCache(mContext, request.mDownloadUrl, request.getHeaders());
                    String cacheFile = null;
                    InputStream is = InternetClient.getInstance(mContext).downloadFile(request.mDownloadUrl,
                                                                   onCheckRequestHeaders(request.mDownloadUrl, request.getHeaders()));
                    if (is != null) {
                        cacheFile = onInputStreamReturn(request.mDownloadUrl, is);
                    }

                    if (DEBUG) {
                        CoreConfig.LOGD("----- after get the cache file : " + cacheFile + " =======");
                    }
                    if (!TextUtils.isEmpty(cacheFile)) {
                        // 将文件移动到下载完成的页面
                        String filePath = mvFileToDownloadedDir(cacheFile,
                                                                   request.mFileExtension);
                        if (!TextUtils.isEmpty(filePath)) {
                            // notify success
                            // 将文件移动到下载完成的页面
                            DownloadResponse response = tryToHandleDownloadFile(filePath, request);
                            if (response != null) {
                                mSuccessHandler.notifyAll(-1, -1, response);
                                handleResponseByListener(DOWNLOAD_SUCCESS, request.mDownloadUrl, response, false);
                                removeRequest(request);
                                continue;
                            } else {
                                handleResponseByListener(DOWNLOAD_FAILED, request.mDownloadUrl, request, false);
                                mFailedHandler.notifyAll(-1, -1, request);
                                continue;
                            }
                        } else {
                            handleResponseByListener(DOWNLOAD_FAILED, request.mDownloadUrl, request, false);
                            mFailedHandler.notifyAll(-1, -1, request);
                            continue;
                        }
                    }

                    if (request.getmStatus() != DownloadRequest.STATUS_CANCEL) {
                        handleResponseByListener(DOWNLOAD_FAILED, request.mDownloadUrl, request, false);
                        mFailedHandler.notifyAll(-1, -1, request);
                    } else {
                        handleResponseByListener(DOWNLOAD_FAILED, request.mDownloadUrl, request, true);
                    }

                    if (DEBUG) {
                        CoreConfig.LOGD("success end operate one request : " + request);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (DEBUG) {
                    CoreConfig.LOGD("Exception : ", e);
                    CoreConfig.LOGD("exception end operate one request : " + request);
                    CoreConfig.LOGD(e.getStackTrace().toString());
                }

                if (request.getmStatus() != DownloadRequest.STATUS_CANCEL) {
                    handleResponseByListener(DOWNLOAD_FAILED,
                                                request.mDownloadUrl, request, false);
                    mFailedHandler.notifyAll(-1, -1, request);
                } else {
                    handleResponseByListener(DOWNLOAD_FAILED,
                                                request.mDownloadUrl, request, true);
                }
            }

            removeRequest(request);
        }

        System.gc();
    }

    private void handleResponseByListener(int status, String fetchUrl,
                                          Object notfiyObj, boolean ignoreNotify) {
        if (mListenerList.size() > 0) {
            int curCode = fetchUrl.hashCode();
            LinkedList<DownloadListenerObj> removeObj = new LinkedList<DownloadListenerObj>();
            synchronized (mListenerList) {
                for (DownloadListenerObj d : mListenerList) {
                    if (d.code == curCode) {
                        if (!ignoreNotify) {
                            d.mDownloadListener.onDownloadFinished(status, notfiyObj);
                        }
                        removeObj.add(d);
                    }
                }
                mListenerList.removeAll(removeObj);
            }
        }
    }

    private DownloadRequest findRequestCanOperate(
                                                     ArrayList<DownloadRequest> requestList) {
        if (DEBUG) {
            CoreConfig.LOGD("<<<<< [[findRequestCanOperate]] >>>>>");
        }

        synchronized (requestList) {
            for (DownloadRequest r : requestList) {
                if (!r.requestIsOperating.get()) {
                    r.requestIsOperating.set(true);

                    if (DEBUG) {
                        CoreConfig.LOGD("<<<<< [[findRequestCanOperate]] end findRequestCanOperate >>>>>");
                    }
                    return r;
                }
            }

            return null;
        }
    }

    private void removeRequest(DownloadRequest r) {
        synchronized (mRequestList) {
            mRequestList.remove(r);
        }
    }

    private void handleProcess(String requestUrl, int fileSize, int downloadSize) {
        int hashCode = requestUrl.hashCode();
        for (DownloadListenerObj l : mListenerList) {
            if (l.code == hashCode && l.mDownloadListener != null) {
                // TODO: should lock the add by main thread and sub thread
                l.mDownloadListener.onDownloadProcess(fileSize, downloadSize);
            }
        }
    }

    private DownloadRequest findCacelRequest(String requestUrl) {
        int hashCode = requestUrl.hashCode();
        synchronized (mRequestList) {
            for (DownloadRequest r : mRequestList) {
                if (r.mUrlHashCode == hashCode) {
                    return r;
                }
            }
        }

        return null;
    }

    @Override
    protected void init(Context context) {
        INPUT_STREAM_CACHE_PATH = Environment.getExternalStorageDirectory() + "/.corelib_filedownload/stream_cache/";
        DOWNLOADED_FILE_DIR = Environment.getExternalStorageDirectory() + "/.corelib_filedownload/";
        mContext = context.getApplicationContext();
        mRequestList = new ArrayList<DownloadRequest>();
        bIsStop = false;
        mKeepAlive = DEFAULT_KEEPALIVE;
        mListenerList = new LinkedList<DownloadListenerObj>();
    }

    /**
     * 将下载好的文件从缓存目录移动到下载完成的目录
     *
     * @param cachedFile
     * @param extension
     * @return
     */
    private String mvFileToDownloadedDir(String cachedFile, String extension) {
        CoreConfig.LOGD("----- move cached file to + " + DOWNLOADED_FILE_DIR);
        File dir = new File(DOWNLOADED_FILE_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.delete();
            dir.mkdirs();
        }

        File file = new File(cachedFile);
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        String ext = TextUtils.isEmpty(extension) ? "" : extension;
        if (cachedFile.endsWith(ext)) {
            ext = "";
        }
        File newFile = new File(dir.getAbsolutePath() + "/" + file.getName() + ext);
//		// 重命名成功
        if (isExternalStorageAvailable() && file.renameTo(newFile)) {
            CoreConfig.LOGD("----- move cached file to + " + newFile.getAbsolutePath() + " successfully InstallApp=======");
            return newFile.getAbsolutePath();
        }

        // 如果重命名失败，则通过拷贝实现

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(file);
            os = null;
            if (dir.getAbsolutePath().startsWith("/data/data")) {
                CoreConfig.LOGD("open world readable file"
                                    + " successfully InstallApp=======" + file.getName() + ext);
                os = mContext.openFileOutput(file.getName() + ext, Context.MODE_WORLD_READABLE);
            } else {
                CoreConfig.LOGD("new FileOutputStream, InstallApp");
                os = new FileOutputStream(newFile);
            }
            CoreConfig.LOGD("----- move cached file to + "
                                + newFile.getAbsolutePath()
                                + " successfully InstallApp=======");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (FileUtil.copy(is, os)) {
            file.delete();
            return newFile.getAbsolutePath();
        }

        return null;
    }

    private static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        boolean externalStorageAvailable = false;
        boolean externalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            externalStorageAvailable = externalStorageWriteable = false;
        }

        if (externalStorageAvailable == true
                && externalStorageWriteable == true) {
            File sdcard = Environment.getExternalStorageDirectory();
            return sdcard == null ? false : (sdcard.canWrite() ? true : false);
        } else {
            return false;
        }
    }

}

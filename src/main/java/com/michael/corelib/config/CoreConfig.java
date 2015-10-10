package com.michael.corelib.config;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.michael.corelib.BuildConfig;
import com.michael.corelib.corelog.CoreLog;
import com.michael.corelib.corelog.DebugLog;
import com.michael.corelib.coreutils.SingleInstanceManager;

import java.io.File;

/**
 * Created by michael on 15/1/6.
 */
public class CoreConfig {

    public static final String TAG = "corelib";

    public static String ROOT_DIR = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
            ? Environment.getExternalStorageDirectory().getAbsolutePath() + "/.corelib_log"
            : null;

    public static boolean DEBUG = new File(ROOT_DIR, ".log").exists();

    public static boolean CORE_LIB_INIT;

    private static DebugLog DEFAULT_DEBUG_LOG;
    private static String mLogFileName;

    public static void init(Context context) {
        if (context == null) {
            return;
        }

        init(context, false);
    }

    public static void init(Context context, boolean debug) {
        if (context == null) {
            return;
        }
        init(context, debug, "");
    }


    /**
     * 初始化corelib
     *
     * @param context
     * @param debug       表示是将corelib调整成debug状态
     * @param logFileName log写入的文件名
     */
    public static void init(Context context, boolean debug, String logFileName) {
        if (context == null) {
            return;
        }

        ROOT_DIR = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                ? Environment.getExternalStorageDirectory().getAbsolutePath() + "/."
                + com.michael.corelib.coreutils.Environment.getPackageName(context)
                : ROOT_DIR;
        DEBUG = debug;
        SingleInstanceManager.getInstance().init(context);

        if (DEBUG) {
            mLogFileName = logFileName;
            DEFAULT_DEBUG_LOG = CoreLog.getInstance().getDebugLogByFileName(logFileName);
        } else {
            DEFAULT_DEBUG_LOG = null;
            CoreLog.getInstance().clearDebugLogFileObj();
        }
        CORE_LIB_INIT = true;
    }

    public static String getLogPath () {

        if (TextUtils.isEmpty(mLogFileName)){
            mLogFileName = "debug_log.txt";
        }
        return new File(ROOT_DIR,mLogFileName).getAbsolutePath();

    }


    public static void LOGD(String msg) {
        if (DEBUG) {
            if (DEFAULT_DEBUG_LOG == null) {
                throw new IllegalArgumentException("please invoke CoreConfig.init before LOGD");
            }

            DEFAULT_DEBUG_LOG.d("", msg);
        }
    }

    public static void LOGD(String tag, String msg) {
        if (DEBUG) {
            if (DEFAULT_DEBUG_LOG == null) {
                throw new IllegalArgumentException("please invoke CoreConfig.init before LOGD");
            }

            DEFAULT_DEBUG_LOG.d(tag, msg);
        }
    }

    public static void LOGD(String msg, Throwable e) {
        if (DEBUG) {
            if (DEFAULT_DEBUG_LOG == null) {
                throw new IllegalArgumentException("please invoke CoreConfig.init before LOGD");
            }

            DEFAULT_DEBUG_LOG.d("", msg, e);
        }
    }

    public static void LOGD(String tag, String msg, Throwable e) {
        if (DEBUG) {
            if (DEFAULT_DEBUG_LOG == null) {
                throw new IllegalArgumentException("please invoke CoreConfig.init before LOGD");
            }

            DEFAULT_DEBUG_LOG.d(tag, msg, e);
        }
    }


    public static void LOGW(String msg, Throwable e) {
        if (DEBUG) {
            if (DEFAULT_DEBUG_LOG == null) {
                throw new IllegalArgumentException("please invoke CoreConfig.init before LOGW");
            }

            DEFAULT_DEBUG_LOG.w("", msg, e);
        }
    }

    public static void LOGW(String tag, String msg, Throwable e) {
        if (DEBUG) {
            if (DEFAULT_DEBUG_LOG == null) {
                throw new IllegalArgumentException("please invoke CoreConfig.init before LOGW");
            }

            DEFAULT_DEBUG_LOG.w(tag, msg, e);
        }
    }

    public static void LOGW(String tag, String msg) {
        if (DEBUG) {
            if (DEFAULT_DEBUG_LOG == null) {
                throw new IllegalArgumentException("please invoke CoreConfig.init before LOGW");
            }

            DEFAULT_DEBUG_LOG.w(tag, msg);
        }
    }


    public static final class VERSION {

        public static final String SDK_VERSION = BuildConfig.SDK_VERSION;

    }

}

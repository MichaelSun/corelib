package com.michael.corelib.config;

import android.content.Context;
import android.os.Environment;
import com.michael.corelib.corelog.CoreLog;
import com.michael.corelib.corelog.DebugLog;
import com.michael.corelib.coreutils.SingleInstanceManager;
import com.michael.corelib.internet.NetworkLog;

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

    /**
     * 初始化corelib
     *
     * @param context
     * @param debug   表示是将corelib调整成debug状态
     */
    public static void init(Context context, boolean debug) {
        if (context == null) {
            return;
        }

        ROOT_DIR = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                       ? Environment.getExternalStorageDirectory().getAbsolutePath() + "/."
                             + com.michael.corelib.coreutils.Environment.getPackageName(context)
                       : ROOT_DIR;
        if (debug) {
            DEBUG = debug;
        }

        DEFAULT_DEBUG_LOG = CoreLog.getInstance().getDebugLogByFileName("");
        //初始化SingleManager
        SingleInstanceManager.getInstance().init(context);
        NetworkLog.DEBUG = DEBUG;
        LOGD("[[CoreConfig::init]] ROOT_DIR = " + ROOT_DIR);

        CORE_LIB_INIT = true;
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

    public static final class VERSION {

        public static final String SDK_VERSION = "15.09.08";

    }

}

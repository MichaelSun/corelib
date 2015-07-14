package com.michael.corelib.config;

import android.content.Context;
import android.os.Environment;
import com.michael.corelib.coreutils.SingleInstanceManager;
import com.michael.corelib.log.DebugLog;

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

    /**
     * 初始化corelib
     *
     * @param context
     * @param debug 表示是将corelib调整成debug状态
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

        //初始化SingleManager
        SingleInstanceManager.getInstance().init(context);

        CORE_LIB_INIT = true;
    }

    public static void LOGD(String msg) {
        if (DEBUG) {
            DebugLog.d("", msg);
        }
    }

    public static void LOGD(String tag, String msg) {
        if (DEBUG) {
            DebugLog.d(tag, msg);
        }
    }

    public static void LOGD(String msg, Throwable e) {
        if (DEBUG) {
            DebugLog.d("", msg, e);
        }
    }
}

package com.michael.corelib.config;

import android.os.Environment;
import com.michael.corelib.log.DebugLog;

import java.io.File;

/**
 * Created by michael on 15/1/6.
 */
public class CoreConfig {

    public static final String TAG = "corelib";

    public static final String LOG_DIR = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                                             ? Environment.getExternalStorageDirectory().getAbsolutePath() + "/.corelib_log"
                                             : null;

    public static final boolean DEBUG = new File(LOG_DIR, ".log").exists();
//    public static final boolean DEBUG = false;

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

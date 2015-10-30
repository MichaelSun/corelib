package com.michael.corelib.corelog;

import android.text.TextUtils;

import com.michael.corelib.config.CoreConfig;

import java.util.HashMap;

/**
 * Created by michael on 15/7/6.
 */
public class CoreLog {

    private static CoreLog gCoreLog = new CoreLog();

    private HashMap<String, DebugLog> mDebugLogMap = new HashMap<String, DebugLog>();

    public static final CoreLog getInstance() {
        return gCoreLog;
    }

    private CoreLog() {
    }

    public DebugLog getDebugLogByFileName(String path,String logFileName) {
        if (TextUtils.isEmpty(logFileName)) {
            logFileName = "debug_log.txt";
        }

        DebugLog debugLog = mDebugLogMap.get(path + "/" + logFileName);
        if (debugLog == null) {
            debugLog = new DebugLog(path,logFileName);
            mDebugLogMap.put(path + "/" + logFileName, debugLog);
        }

        return debugLog;
    }

    public void clearDebugLogFileObj() {
        mDebugLogMap.clear();
    }
}

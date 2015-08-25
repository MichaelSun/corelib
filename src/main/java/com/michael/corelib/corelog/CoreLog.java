package com.michael.corelib.corelog;

import android.text.TextUtils;

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

    public DebugLog getDebugLogByFileName(String logFileName) {
        if (TextUtils.isEmpty(logFileName)) {
            logFileName = "debug_log.txt";
        }

        DebugLog debugLog = mDebugLogMap.get(logFileName);
        if (debugLog == null) {
            debugLog = new DebugLog(logFileName);
            mDebugLogMap.put(logFileName, debugLog);
        }

        return debugLog;
    }
}

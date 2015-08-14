package com.michael.corelib.internet;

import com.michael.corelib.config.CoreConfig;

/**
 * Created by michael on 15/1/6.
 */
public class NetworkLog {

    public static boolean DEBUG = CoreConfig.DEBUG;

    public static final int SIG_PARAM_MAX_LENGTH = 5000;

    public static void LOGD(String msg) {
        CoreConfig.LOGD(msg);
    }

}

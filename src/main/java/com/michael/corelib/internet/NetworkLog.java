package com.michael.corelib.internet;

import com.michael.corelib.config.CoreConfig;

/**
 * Created by michael on 15/1/6.
 */
public class NetworkLog {

    public static void LOGD(String msg) {
        if (CoreConfig.DEBUG) {
            try {
                CoreConfig.LOGD(msg);
            } catch (Exception e) {
                //do nothing
            }

        }
    }

}

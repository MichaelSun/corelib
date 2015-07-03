/**
 * Environment.java
 */
package com.michael.corelib.coreutils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Guoqing Sun Feb 18, 20133:16:14 PM
 */
public final class Environment {

    private static final String DEBUG_DATE_FORMAT = "MM-dd HH:mm:ss:SSS";

    public static String debugFormatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEBUG_DATE_FORMAT);
        return dateFormat.format(time);
    }

    public static String getPackageName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.packageName; // 包名
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    public static String getIMSI(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = mTelephonyMgr.getSubscriberId();

        return imsi;
    }

    public static String getIMEI(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mTelephonyMgr.getDeviceId();
        return imei;
    }

    public static boolean isSDCardReady() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (info != null) {
            return info.getMacAddress();
        }

        return null;
    }

    public static String getVersionName(Context context) {
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            String version = packInfo.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "0.0";
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        // String versionName=null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess == null) {
                continue;
            }
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}

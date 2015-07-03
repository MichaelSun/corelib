/**
 * DiskManager.java
 */
package com.michael.corelib.coreutils;


import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * @author Guoqing Sun Oct 24, 20122:42:55 PM
 */
public class DiskManager {

	/**
	 * dubbler的cache目录，所有的cache文件都存储在此目录下
	 */
	private static String DISK_DIR = Environment.getExternalStorageDirectory() + "/.corelibDefaultDisk/";

	private static final long MAX_DISK_SIZE = ((long) 100) * 1024 * 1024;
	private static final long MAX_FLASH_SIZE = ((long) 5) * 1024 * 1024;

	/**
	 * 尝试根据类型获取路径, 如果不存在会创建此路径
	 */
	public static String tryToFetchPath(Context context, String subDir) {
        if (context != null) {
            String packageName = context.getPackageName();
            if (!TextUtils.isEmpty(packageName)) {
                DISK_DIR = Environment.getExternalStorageDirectory() + "/." + packageName + "/";
            }
        }

		String retDir = DISK_DIR + subDir;
		File dirCheck = new File(retDir);
		if (dirCheck.exists() && !dirCheck.isDirectory()) {
			dirCheck.delete();
		}

		if (!dirCheck.exists()) {
			dirCheck.mkdirs();
		}

		return retDir;
	}

}

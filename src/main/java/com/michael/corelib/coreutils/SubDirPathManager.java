package com.michael.corelib.coreutils;

import android.content.Context;
import com.michael.corelib.config.CoreConfig;

import java.io.File;

/**
 * @author Guoqing Sun Oct 24, 20122:42:55 PM
 */
public class SubDirPathManager {

	private static String DISK_DIR = CoreConfig.ROOT_DIR;

	private static final long MAX_DISK_SIZE = ((long) 100) * 1024 * 1024;
	private static final long MAX_FLASH_SIZE = ((long) 5) * 1024 * 1024;

	/**
	 * 尝试根据类型获取路径, 如果不存在会创建此路径
	 */
	public static String tryToFetchPath(Context context, String subDir) {
        if (context != null) {
			if (!CoreConfig.CORE_LIB_INIT) {
				CoreConfig.init(context, false);
			}
			DISK_DIR = CoreConfig.ROOT_DIR + File.separator;
        }

		String retDir = DISK_DIR + subDir + File.separator;
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

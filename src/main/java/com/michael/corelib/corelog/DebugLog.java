/**
 * Copyright (C)  2007 - 2010
 */

package com.michael.corelib.corelog;

import android.text.TextUtils;
import android.util.Log;
import com.michael.corelib.config.CoreConfig;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 
 * dump the log to sdcard file, should replace Config.LOG by this step by step
 * 
 */
public class DebugLog {

	private static final String TAG = "DebugLog";

	private static final String LOG_DIR = CoreConfig.ROOT_DIR;
	private String LOG_CURR_FILENAME = "debug_log.txt";

	private static final long MAX_LOGFILE_SIZE = 2 * 1024 * 1024;

	/**
	 * flag to control if dump the log to console
	 */
	private static final boolean DUMP_LOG_TO_CONSOLE = true;

	private static final String VERBOSE_VERBOSE_TAG = "VV";
	private static final String VERBOSE_TAG = "VERBOSE";
	private static final String DEBUG_TAG = "DEBUG";
	private static final String WARNING_TAG = "WARNIG";
	private static final String ERORR_TAG = "ERROR";
	private static final String RELEASE_TAG = "RELEASE";

	private BufferedWriter mOutWriter = null;
	private long mCurrFileSize = 0;

	private static final String DATE_FORMAT = "MM-dd HH:mm:ss:SSS";
	private SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);
	private Calendar mCalendar = Calendar.getInstance();

	public DebugLog(String logFileName) {
		if (!TextUtils.isEmpty(logFileName)) {
			LOG_CURR_FILENAME = logFileName;
		}
	}

	/**
	 * Send a verbose log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public void vv(String tag, String msg) {
		vv(tag, msg, null);
	}

	/**
	 * Send a verbose log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public void vv(String tag, String msg, Throwable tr) {
		writeLog(VERBOSE_VERBOSE_TAG, tag, msg, tr);
        if (DUMP_LOG_TO_CONSOLE) {
            if (tag != null && !tag.equals("")) {
                Log.d(TAG, "[[" + tag + "]]" + msg, tr);
            } else {
                Log.d(TAG, msg, tr);
            }
        }
	}

	/**
	 * Send a verbose log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public void v(String tag, String msg) {
		v(tag, msg, null);
	}

	/**
	 * Send a verbose log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public void v(String tag, String msg, Throwable tr) {
		writeLog(VERBOSE_TAG, tag, msg, tr);
        if (DUMP_LOG_TO_CONSOLE) {
            if (tag != null && !tag.equals("")) {
                Log.v(TAG, "[[" + tag + "]]" + msg, tr);
            } else {
                Log.v(TAG, msg, tr);
            }
        }
	}

	/**
	 * Send a debug log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public void d(String tag, String msg) {
		d(tag, msg, null);
	}

	/**
	 * Send a debug log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public void d(String tag, String msg, Throwable tr) {
		writeLog(DEBUG_TAG, tag, msg, tr);
		if (DUMP_LOG_TO_CONSOLE) {
            if (tag != null && !tag.equals("")) {
                Log.d(TAG, "[[" + tag + "]]" + msg, tr);
            } else {
                Log.d(TAG, msg, tr);
            }
		}
	}

	/**
	 * Send a warning log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public void w(String tag, String msg) {
		w(tag, msg, null);
	}

	/**
	 * Send a warning log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public void w(String tag, String msg, Throwable tr) {
		writeLog(WARNING_TAG, tag, msg, tr);
        if (DUMP_LOG_TO_CONSOLE) {
            if (tag != null && !tag.equals("")) {
                Log.w(TAG, "[[" + tag + "]]" + msg, tr);
            } else {
                Log.w(TAG, msg, tr);
            }
        }
	}

	/**
	 * Send a error log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public void e(String tag, String msg) {
		e(tag, msg, null);
	}

	/**
	 * Send a error log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public void e(String tag, String msg, Throwable tr) {
		writeLog(ERORR_TAG, tag, msg, tr);
        if (DUMP_LOG_TO_CONSOLE) {
            if (tag != null && !tag.equals("")) {
                Log.e(TAG, "[[" + tag + "]]" + msg, tr);
            } else {
                Log.e(TAG, msg, tr);
            }
        }
	}

	/**
	 * Send a release log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public void r(String tag, String msg) {
		r(tag, msg, null);
	}

	/**
	 * Send a release log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public void r(String tag, String msg, Throwable tr) {
		writeLog(RELEASE_TAG, tag, msg, tr);
        if (DUMP_LOG_TO_CONSOLE) {
            if (tag != null && !tag.equals("")) {
                Log.e(TAG, "[[" + tag + "]]" + msg, tr);
            } else {
                Log.e(TAG, msg, tr);
            }
        }
	}

	public void close() {
		if (null != mOutWriter) {
			try {
				mOutWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mOutWriter = null;
	}

	private synchronized void writeLog(String level, String tag, String msg,
			Throwable tr) {
		if (null == mOutWriter) {
			boolean success = openFile();
			if (!success) {
				return;
			}
		}

		StringBuilder log = composeLog(level, tag, msg, tr);
		mCurrFileSize += log.length();

		try {
			mOutWriter.write(log.toString());
			mOutWriter.flush();
			mOutWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}

		if (mCurrFileSize > MAX_LOGFILE_SIZE) {
			// TODO change log file.
		}
	}

	private boolean openFile() {
		File f = new File(LOG_DIR);

		boolean logDirExists = false;
		if (!f.exists()) {
			logDirExists = f.mkdirs();
		} else {
			logDirExists = true;
		}
		if (!logDirExists) {
			return false;
		}

		f = new File(LOG_DIR, LOG_CURR_FILENAME);
		mOutWriter = null;
		try {
			mOutWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f, true)));
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	private StringBuilder composeLog(String level, String tag, String msg,
			Throwable tr) {
	    StringBuilder log = new StringBuilder(512);

		log.append(composeTime());
		log.append(" ");
		log.append(level + "/" + tag);
		log.append("\t");
		if (!TextUtils.isEmpty(msg)) {
			log.append(msg);
			if (null != tr) {
				log.append("\n\t");
			}
		}
		log.append(Log.getStackTraceString(tr));

		return log;
	}

	private String composeTime() {
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		return mDateFormat.format(mCalendar.getTime());
	}
}

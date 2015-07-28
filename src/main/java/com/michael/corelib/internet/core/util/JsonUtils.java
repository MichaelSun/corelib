package com.michael.corelib.internet.core.util;

import android.text.TextUtils;
import com.michael.corelib.internet.core.json.JsonMapper;

/**
 * Provides methods for converting JSON response string to object.
 * 
 */
public class JsonUtils {

	private static JsonMapper mapper;

	private static Object lockObject = new Object();
	
	/**
	 * Convert JSON response string to corresponding object
	 * 
	 * @param response
	 *            Response string
	 * @param valueType
	 *            The target class
	 * @return The object with type T will be returned if successfully parsing
	 *         the JSON response string, or return null if failed.
	 */
	public static <T> T parse(String response, Class<T> valueType) {
		if (TextUtils.isEmpty(response) || valueType == null) {
			return null;
		}
		if (mapper == null) {
		    synchronized (lockObject) {
		        if (mapper == null) {
		            mapper = new JsonMapper();
		        }
		    }
			// mapper = new ObjectMapper();
		}
		try {
			return mapper.readValue(response, valueType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

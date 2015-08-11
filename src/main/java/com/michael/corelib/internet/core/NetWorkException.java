package com.michael.corelib.internet.core;

public class NetWorkException extends Exception {

    private static final long serialVersionUID = 1L;

    /** 本地网络错误码全部为负数 */
    public static final int NETWORK_NOT_AVILABLE = -1;
    public static final int SERVER_ERROR = -2;
    public static final int NETWORK_ERROR = -3;
    public static final int USER_NOT_LOGIN = -4;
    public static final int PARAM_EMPTY = -5;
    public static final int MISS_API_NAME = -6;
    public static final int MISS_CONTENT = -7;
    public static final int ENCODE_HTTP_PARAMS_ERROR = -8;
    public static final int REQUEST_NULL = -9;
    public static final int RESPONSE_PARSE_ERROR = -10;
    
    public final int exceptionCode;
    public final String developerExceptionMsg;
    public final String userExceptionMsg;

    public final NetworkResponse networkResponse;

    public NetWorkException(int code, String msg, String description, NetworkResponse response) {
        exceptionCode = code;
        developerExceptionMsg = msg;
        userExceptionMsg = description;
        networkResponse = response;
    }

    public NetWorkException(String msg) {
        this(0, msg, null, null);
    }

    @Override
    public String toString() {
        return "NetWorkException{" +
                   "exceptionCode=" + exceptionCode +
                   ", developerExceptionMsg='" + developerExceptionMsg + '\'' +
                   ", userExceptionMsg='" + userExceptionMsg + '\'' +
                   ", networkResponse=" + networkResponse +
                   "} " + super.toString();
    }
}

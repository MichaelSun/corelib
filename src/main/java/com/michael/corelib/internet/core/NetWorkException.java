package com.michael.corelib.internet.core;

public class NetWorkException extends Exception {

    //本地网络错误码全部为负数
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
    
    private static final long serialVersionUID = 1L;
    
    private int mExceptionCode;
    private String mDeveloperExceptionMsg;
    private String mUserExceptionMsg;
    
    public NetWorkException(String exceptionMsg) {
        super(exceptionMsg);
        mDeveloperExceptionMsg = exceptionMsg;
    }
    
    public NetWorkException(int code, String msg, String description) {
        super(msg);
        mExceptionCode = code;
        mDeveloperExceptionMsg = msg;
        mUserExceptionMsg = description;
    }
    
    public int getErrorCode() {
        return mExceptionCode;
    }
    
    public String getDeveloperExceptionMsg() {
        return mDeveloperExceptionMsg;
    }
    
    public String getUserExceptionMsg() {
        return mUserExceptionMsg;
    }
    
    @Override
    public String toString() {
        return "NetWorkException [mExceptionCode=" + mExceptionCode + ", mExceptionMsg=" + mDeveloperExceptionMsg
                + ", mExceptionDescription=" + mUserExceptionMsg + "]";
    }
    
}

package com.michael.corelib.internet.core;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by michael on 15/8/11.
 */
public class NetworkResponse implements Serializable {

    private static final long serialVersionUID = -20150728102000L;

    /**
     * Creates a new network response.
     * @param statusCode the HTTP status code
     * @param data Response body
     * @param headers Headers returned with this response, or null for none
     */
    public NetworkResponse(int statusCode, String data, Header[] headers) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
    }

    public NetworkResponse(String data) {
        this(HttpStatus.SC_OK, data, null);
    }

    public NetworkResponse(String data, Header[] headers) {
        this(HttpStatus.SC_OK, data, headers);
    }

    /** The HTTP status code. */
    public final int statusCode;

    /** Raw data from this response. */
    public final String data;

    /** Response headers. */
    public final Header[] headers;


    @Override
    public String toString() {
        return "NetworkResponse{" +
                   "statusCode=" + statusCode +
                   ", data='" + data + '\'' +
                   ", headers=" + Arrays.toString(headers) +
                   '}';
    }
}

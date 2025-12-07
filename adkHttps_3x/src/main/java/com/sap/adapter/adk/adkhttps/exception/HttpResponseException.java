package com.sap.adapter.adk.adkhttps.exception;

import java.io.PrintWriter;

public class HttpResponseException extends Exception {
    private static final long serialVersionUID = 1L;

    public HttpResponseException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace(PrintWriter wr) {
        wr.println(this.getMessage());
    }
}
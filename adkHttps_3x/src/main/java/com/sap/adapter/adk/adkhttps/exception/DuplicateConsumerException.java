package com.sap.adapter.adk.adkhttps.exception;

import java.io.PrintWriter;

public class DuplicateConsumerException extends Exception {
    private static final long serialVersionUID = 1L;

    public DuplicateConsumerException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace(PrintWriter wr) {
        wr.println(this.getMessage());
    }
}

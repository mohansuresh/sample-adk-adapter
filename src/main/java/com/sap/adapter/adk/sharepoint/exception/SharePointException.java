package com.sap.adapter.adk.sharepoint.exception;

/**
 * Base exception class for SharePoint adapter operations
 */
public class SharePointException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SharePointException(String message) {
        super(message);
    }

    public SharePointException(String message, Throwable cause) {
        super(message, cause);
    }

    public SharePointException(Throwable cause) {
        super(cause);
    }
}

package com.sap.adapter.adk.sharepoint.exception;

/**
 * Exception thrown when authentication with SharePoint fails
 */
public class SharePointAuthenticationException extends SharePointException {

    private static final long serialVersionUID = 1L;

    public SharePointAuthenticationException(String message) {
        super(message);
    }

    public SharePointAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

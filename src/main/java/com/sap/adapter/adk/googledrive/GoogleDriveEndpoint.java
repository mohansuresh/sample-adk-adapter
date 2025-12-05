package com.sap.adapter.adk.googledrive;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultPollingEndpoint;

/**
 * Google Drive Adapter Endpoint for SAP CPI Integration Suite.
 * Represents a Google Drive endpoint with configuration parameters.
 */
@UriEndpoint(scheme = "sap-googledrive", syntax = "", title = "Google Drive Adapter")
public class GoogleDriveEndpoint extends DefaultPollingEndpoint {

    @UriParam
    private String serviceAccountEmail;
    
    @UriParam
    private String credentialName;
    
    @UriParam
    private String operation;
    
    @UriParam
    private String folderId;
    
    @UriParam
    private String fileName;
    
    @UriParam
    private String mimeType;
    
    @UriParam
    private String applicationName;

    public GoogleDriveEndpoint() {
    }

    public GoogleDriveEndpoint(final String endpointUri, final GoogleDriveComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() {
        return new GoogleDriveProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Google Drive Adapter does not support Consumer (Sender) mode in this version");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    // Getters and Setters
    
    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public void setServiceAccountEmail(String serviceAccountEmail) {
        this.serviceAccountEmail = serviceAccountEmail;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}

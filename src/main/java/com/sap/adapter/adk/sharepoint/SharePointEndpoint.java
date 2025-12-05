package com.sap.adapter.adk.sharepoint;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultPollingEndpoint;

/**
 * SharePoint Adapter Endpoint for SAP CPI Integration Suite
 * Supports both Sender (polling) and Receiver operations
 * Compatible with Apache Camel 3.14
 */
@UriEndpoint(scheme = "sap-sharepoint", syntax = "sap-sharepoint:operation", title = "SAP SharePoint Adapter")
public class SharePointEndpoint extends DefaultPollingEndpoint {

    // Connection Parameters
    @UriParam
    private String siteUrl;

    @UriParam
    private String tenantId;

    @UriParam
    private String authenticationType; // OAuth2, OAuth2ClientCredentials, SAML, Dynamic

    @UriParam
    private String credentialName;

    @UriParam
    private String clientId;

    @UriParam
    private String clientSecret;

    @UriParam
    private String tokenEndpoint;

    @UriParam
    private String scope;

    // Operation Parameters
    @UriParam
    private String operation; // Sites, Lists, Files, etc.

    @UriParam
    private String operationName; // Specific operation like GetSite, CreateList, etc.

    @UriParam
    private String responseFormat; // JSON or XML

    @UriParam
    private String listId;

    @UriParam
    private String driveId;

    @UriParam
    private String itemId;

    @UriParam
    private String folderPath;

    @UriParam
    private String fileName;

    // Pagination Parameters
    @UriParam
    private boolean enablePagination = false;

    @UriParam
    private int pageSize = 100;

    @UriParam
    private String skipToken;

    // Post-Processing Parameters (Sender)
    @UriParam
    private String postProcessing; // Delete, Keep, MarkProcessed

    @UriParam
    private boolean mplOnFailure = true;

    @UriParam
    private long maxFileSize = 250 * 1024 * 1024; // 250MB default

    // Proxy Parameters
    @UriParam
    private String proxyHost;

    @UriParam
    private int proxyPort;

    @UriParam
    private String proxyType; // HTTP, SOCKS

    @UriParam
    private String locationId; // For Cloud Connector

    // Timeout Parameters
    @UriParam
    private int connectionTimeout = 30000; // 30 seconds

    @UriParam
    private int socketTimeout = 60000; // 60 seconds

    // Additional Parameters
    @UriParam
    private String queryParameters;

    @UriParam
    private String customHeaders;

    @UriParam
    private boolean traceEnabled = false;

    public SharePointEndpoint() {
    }

    public SharePointEndpoint(String endpointUri, SharePointComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new SharePointProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        SharePointConsumer consumer = new SharePointConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    // Getters and Setters

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isEnablePagination() {
        return enablePagination;
    }

    public void setEnablePagination(boolean enablePagination) {
        this.enablePagination = enablePagination;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSkipToken() {
        return skipToken;
    }

    public void setSkipToken(String skipToken) {
        this.skipToken = skipToken;
    }

    public String getPostProcessing() {
        return postProcessing;
    }

    public void setPostProcessing(String postProcessing) {
        this.postProcessing = postProcessing;
    }

    public boolean isMplOnFailure() {
        return mplOnFailure;
    }

    public void setMplOnFailure(boolean mplOnFailure) {
        this.mplOnFailure = mplOnFailure;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(String customHeaders) {
        this.customHeaders = customHeaders;
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }
}

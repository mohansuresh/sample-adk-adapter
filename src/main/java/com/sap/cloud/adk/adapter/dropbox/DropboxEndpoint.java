package com.sap.cloud.adk.adapter.dropbox;

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dropbox Adapter Endpoint for SAP Cloud Platform Integration
 * Supports both Sender and Receiver operations with Dropbox API v2
 */
@UriEndpoint(
    firstVersion = "1.0.0",
    scheme = "sap-dropbox",
    title = "Dropbox Adapter",
    syntax = "sap-dropbox:operation",
    category = {Category.CLOUD, Category.API},
    headersClass = DropboxConstants.class
)
public class DropboxEndpoint extends DefaultEndpoint {
    
    private static final Logger LOG = LoggerFactory.getLogger(DropboxEndpoint.class);

    @UriPath
    @Metadata(required = true)
    private String operation;

    // Connection Parameters
    @UriParam(label = "security", secret = true)
    @Metadata(required = true, description = "OAuth2 Authorization Code credential alias from Security Material")
    private String credentialAlias;

    @UriParam(defaultValue = "300000")
    @Metadata(description = "Timeout in milliseconds for Dropbox API calls")
    private int timeout = 300000;

    // Sender Parameters
    @UriParam(label = "sender")
    @Metadata(description = "File path for download operation")
    private String filePath;

    @UriParam(label = "sender")
    @Metadata(description = "Folder path for download archive operation")
    private String folderPath;

    @UriParam(label = "sender", defaultValue = "delete")
    @Metadata(description = "Post-processing action: delete, keep, move, idempotent")
    private String postProcessing = "delete";

    @UriParam(label = "sender")
    @Metadata(description = "Archive directory for move post-processing")
    private String archiveDirectory;

    @UriParam(label = "sender", defaultValue = "false")
    @Metadata(description = "Raise exception if post-processing fails")
    private boolean raiseExceptionOnPostProcessingFailure = false;

    @UriParam(label = "sender", defaultValue = "90")
    @Metadata(description = "Persist duration in days for idempotent repository")
    private int persistDuration = 90;

    @UriParam(label = "sender", defaultValue = "60000")
    @Metadata(description = "Polling interval in milliseconds")
    private long pollingInterval = 60000;

    // Receiver Parameters
    @UriParam(label = "receiver")
    @Metadata(description = "Source path for copy/move operations")
    private String sourcePath;

    @UriParam(label = "receiver")
    @Metadata(description = "Destination path for copy/move/upload operations")
    private String destinationPath;

    @UriParam(label = "receiver", defaultValue = "fail")
    @Metadata(description = "Handling of existing files: autoRename, fail, ignore, overwrite, dynamic")
    private String handlingExistingFiles = "fail";

    @UriParam(label = "receiver", defaultValue = "false")
    @Metadata(description = "Include deleted files in metadata")
    private boolean includeDeleted = false;

    @UriParam(label = "receiver", defaultValue = "false")
    @Metadata(description = "Recursive listing for folders")
    private boolean recursive = false;

    @UriParam(label = "receiver", defaultValue = "1000")
    @Metadata(description = "Limit for list operations")
    private int limit = 1000;

    @UriParam(label = "receiver", defaultValue = "path")
    @Metadata(description = "Mode for list revisions: path or id")
    private String mode = "path";

    @UriParam(label = "receiver")
    @Metadata(description = "Template ID for update metadata operation")
    private String templateId;

    @UriParam(label = "receiver")
    @Metadata(description = "Add or update fields for metadata")
    private String addOrUpdateFields;

    @UriParam(label = "receiver")
    @Metadata(description = "Remove fields for metadata")
    private String removeFields;

    @UriParam(label = "receiver")
    @Metadata(description = "Search query string")
    private String query;

    @UriParam(label = "receiver")
    @Metadata(description = "Search path scope")
    private String searchPath;

    @UriParam(label = "receiver", defaultValue = "1000")
    @Metadata(description = "Maximum results for search")
    private int maxResults = 1000;

    @UriParam(label = "receiver", defaultValue = "relevance")
    @Metadata(description = "Order by: relevance or modifiedTime")
    private String orderBy = "relevance";

    @UriParam(label = "receiver", defaultValue = "false")
    @Metadata(description = "Mute notifications for file modifications")
    private boolean mute = false;

    @UriParam(label = "receiver", defaultValue = "json")
    @Metadata(description = "Response format: json or xml")
    private String responseFormat = "json";

    @UriParam(label = "receiver")
    @Metadata(description = "Request headers (pipe-separated)")
    private String requestHeaders;

    @UriParam(label = "receiver")
    @Metadata(description = "Response headers (pipe-separated)")
    private String responseHeaders;

    public DropboxEndpoint(String uri, DropboxComponent component) {
        super(uri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        LOG.debug("Creating Dropbox Producer for operation: {}", operation);
        return new DropboxProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        LOG.debug("Creating Dropbox Consumer for operation: {}", operation);
        DropboxConsumer consumer = new DropboxConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    // Getters and Setters
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getCredentialAlias() {
        return credentialAlias;
    }

    public void setCredentialAlias(String credentialAlias) {
        this.credentialAlias = credentialAlias;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getPostProcessing() {
        return postProcessing;
    }

    public void setPostProcessing(String postProcessing) {
        this.postProcessing = postProcessing;
    }

    public String getArchiveDirectory() {
        return archiveDirectory;
    }

    public void setArchiveDirectory(String archiveDirectory) {
        this.archiveDirectory = archiveDirectory;
    }

    public boolean isRaiseExceptionOnPostProcessingFailure() {
        return raiseExceptionOnPostProcessingFailure;
    }

    public void setRaiseExceptionOnPostProcessingFailure(boolean raiseExceptionOnPostProcessingFailure) {
        this.raiseExceptionOnPostProcessingFailure = raiseExceptionOnPostProcessingFailure;
    }

    public int getPersistDuration() {
        return persistDuration;
    }

    public void setPersistDuration(int persistDuration) {
        this.persistDuration = persistDuration;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public String getHandlingExistingFiles() {
        return handlingExistingFiles;
    }

    public void setHandlingExistingFiles(String handlingExistingFiles) {
        this.handlingExistingFiles = handlingExistingFiles;
    }

    public boolean isIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getAddOrUpdateFields() {
        return addOrUpdateFields;
    }

    public void setAddOrUpdateFields(String addOrUpdateFields) {
        this.addOrUpdateFields = addOrUpdateFields;
    }

    public String getRemoveFields() {
        return removeFields;
    }

    public void setRemoveFields(String removeFields) {
        this.removeFields = removeFields;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSearchPath() {
        return searchPath;
    }

    public void setSearchPath(String searchPath) {
        this.searchPath = searchPath;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
}

package com.sap.it.custom.dropbox;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultPollingEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dropbox Endpoint for SAP Cloud Platform Integration.
 * Represents a Dropbox connection endpoint with configuration parameters.
 * 
 * @author SAP CPI Custom Adapter
 * @version 1.0.0
 */
@UriEndpoint(scheme = "sap-dropbox", syntax = "sap-dropbox:operation", title = "Dropbox Adapter")
public class DropboxEndpoint extends DefaultPollingEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxEndpoint.class);

    // Connection Parameters
    @UriParam(description = "OAuth2 credential alias name from Security Material")
    private String credentialName;

    @UriParam(description = "Timeout in milliseconds", defaultValue = "300000")
    private int timeout = 300000;

    // Operation Parameters
    @UriParam(description = "Dropbox operation to perform")
    private String operation;

    // Sender Adapter Parameters
    @UriParam(description = "File path for download operation")
    private String filePath;

    @UriParam(description = "Folder path for download archive operation")
    private String folderPath;

    @UriParam(description = "Post-processing action: Delete, Archive, KeepAndProcess, KeepAndMark", defaultValue = "KeepAndProcess")
    private String postProcessing = "KeepAndProcess";

    @UriParam(description = "Archive directory path for move operation")
    private String archiveDirectory;

    @UriParam(description = "Raise exception on post-processing failure", defaultValue = "false")
    private boolean raiseExceptionOnPostProcessingFailure = false;

    @UriParam(description = "Persist duration in days for idempotent repository", defaultValue = "90")
    private int persistDuration = 90;

    // Receiver Adapter Parameters
    @UriParam(description = "Source path for copy/move operations")
    private String sourcePath;

    @UriParam(description = "Destination path for copy/move operations")
    private String destinationPath;

    @UriParam(description = "Handling of existing files: AutoRename, Fail, Ignore, Overwrite, Dynamic", defaultValue = "Fail")
    private String handlingExistingFiles = "Fail";

    @UriParam(description = "Include deleted files in metadata", defaultValue = "false")
    private boolean includeDeleted = false;

    @UriParam(description = "Recursive listing for folders", defaultValue = "false")
    private boolean recursive = false;

    @UriParam(description = "Limit for list operations", defaultValue = "1000")
    private int limit = 1000;

    @UriParam(description = "Mode for list revisions: path or id", defaultValue = "path")
    private String mode = "path";

    @UriParam(description = "Template ID for metadata operations")
    private String templateId;

    @UriParam(description = "Add or update fields for metadata (JSON format)")
    private String addOrUpdateFields;

    @UriParam(description = "Remove fields for metadata (comma-separated)")
    private String removeFields;

    @UriParam(description = "Search query string")
    private String query;

    @UriParam(description = "Max results for search", defaultValue = "1000")
    private int maxResults = 1000;

    @UriParam(description = "Order by: relevance or modifiedTime", defaultValue = "relevance")
    private String orderBy = "relevance";

    @UriParam(description = "Mute notifications", defaultValue = "false")
    private boolean mute = false;

    @UriParam(description = "Response format: JSON or XML", defaultValue = "JSON")
    private String responseFormat = "JSON";

    @UriParam(description = "Request headers (pipe-separated)")
    private String requestHeaders;

    @UriParam(description = "Response headers (pipe-separated)")
    private String responseHeaders;

    /**
     * Default constructor.
     */
    public DropboxEndpoint() {
    }

    /**
     * Constructor with URI and component.
     *
     * @param endpointUri The endpoint URI
     * @param component   The Dropbox component
     */
    public DropboxEndpoint(String endpointUri, DropboxComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        LOG.info("Creating Dropbox Producer for operation: {}", operation);
        return new DropboxProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        LOG.info("Creating Dropbox Consumer for operation: {}", operation);
        DropboxConsumer consumer = new DropboxConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    // Getters and Setters

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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

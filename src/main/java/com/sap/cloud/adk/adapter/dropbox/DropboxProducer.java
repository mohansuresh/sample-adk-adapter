package com.sap.cloud.adk.adapter.dropbox;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Dropbox Producer for Receiver operations
 * Handles all outbound operations to Dropbox API
 */
public class DropboxProducer extends DefaultProducer {
    
    private static final Logger LOG = LoggerFactory.getLogger(DropboxProducer.class);
    
    private final DropboxEndpoint endpoint;
    
    public DropboxProducer(DropboxEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }
    
    @Override
    public void process(Exchange exchange) throws Exception {
        String operation = endpoint.getOperation();
        LOG.info("Processing Dropbox receiver operation: {}", operation);
        
        // Get access token from credential alias
        String accessToken = getAccessToken();
        
        // Get timeout from header or endpoint
        int timeout = getTimeout(exchange);
        
        // Create Dropbox client
        DropboxClient client = new DropboxClient(accessToken, timeout);
        
        // Execute operation
        String response;
        switch (operation) {
            case DropboxConstants.OPERATION_COPY:
                response = executeCopy(exchange, client);
                break;
            case DropboxConstants.OPERATION_CREATE_FOLDER:
                response = executeCreateFolder(exchange, client);
                break;
            case DropboxConstants.OPERATION_DELETE:
                response = executeDelete(exchange, client);
                break;
            case DropboxConstants.OPERATION_GET_FILE_URL:
                response = executeGetFileUrl(exchange, client);
                break;
            case DropboxConstants.OPERATION_GET_METADATA:
                response = executeGetMetadata(exchange, client);
                break;
            case DropboxConstants.OPERATION_GET_STORAGE_STATS:
                response = executeGetStorageStats(exchange, client);
                break;
            case DropboxConstants.OPERATION_LIST_FOLDER:
                response = executeListFolder(exchange, client);
                break;
            case DropboxConstants.OPERATION_LIST_REVISIONS:
                response = executeListRevisions(exchange, client);
                break;
            case DropboxConstants.OPERATION_MOVE:
                response = executeMove(exchange, client);
                break;
            case DropboxConstants.OPERATION_UPDATE_METADATA:
                response = executeUpdateMetadata(exchange, client);
                break;
            case DropboxConstants.OPERATION_SEARCH:
                response = executeSearch(exchange, client);
                break;
            case DropboxConstants.OPERATION_UPLOAD:
                response = executeUpload(exchange, client);
                break;
            default:
                throw new IllegalArgumentException(DropboxConstants.ERROR_INVALID_OPERATION + ": " + operation);
        }
        
        // Convert response format if needed
        String responseFormat = getResponseFormat(exchange);
        if (DropboxConstants.FORMAT_XML.equalsIgnoreCase(responseFormat)) {
            response = client.convertJsonToXml(response);
        }
        
        // Set response in exchange
        exchange.getMessage().setBody(response);
        
        LOG.info("Dropbox operation completed successfully: {}", operation);
    }
    
    /**
     * Execute copy file or folder operation
     */
    private String executeCopy(Exchange exchange, DropboxClient client) throws Exception {
        String sourcePath = getParameter(exchange, "sourcePath", endpoint.getSourcePath());
        String destinationPath = getParameter(exchange, "destinationPath", endpoint.getDestinationPath());
        String handling = getHandlingExistingFiles(exchange);
        
        validateRequired(sourcePath, "sourcePath");
        validateRequired(destinationPath, "destinationPath");
        
        Map<String, Object> params = new HashMap<>();
        params.put("from_path", sourcePath);
        params.put("to_path", destinationPath);
        params.put("autorename", DropboxConstants.HANDLING_AUTO_RENAME.equalsIgnoreCase(handling));
        
        LOG.debug("Copying from {} to {}", sourcePath, destinationPath);
        
        String response = client.executeApiCall(DropboxConstants.API_COPY, params);
        
        // Handle ignore case
        if (DropboxConstants.HANDLING_IGNORE.equalsIgnoreCase(handling)) {
            // Check if file exists and ignore if it does
            try {
                return response;
            } catch (Exception e) {
                if (e.getMessage().contains("conflict")) {
                    LOG.info("File already exists, ignoring as per configuration");
                    return "{\"status\":\"ignored\"}";
                }
                throw e;
            }
        }
        
        return response;
    }
    
    /**
     * Execute create folder operation
     */
    private String executeCreateFolder(Exchange exchange, DropboxClient client) throws Exception {
        String folderPath = getParameter(exchange, "folderPath", endpoint.getFolderPath());
        String handling = getHandlingExistingFiles(exchange);
        
        validateRequired(folderPath, "folderPath");
        
        Map<String, Object> params = new HashMap<>();
        params.put("path", folderPath);
        params.put("autorename", DropboxConstants.HANDLING_AUTO_RENAME.equalsIgnoreCase(handling));
        
        LOG.debug("Creating folder: {}", folderPath);
        
        try {
            return client.executeApiCall(DropboxConstants.API_CREATE_FOLDER, params);
        } catch (Exception e) {
            if (DropboxConstants.HANDLING_IGNORE.equalsIgnoreCase(handling) && e.getMessage().contains("conflict")) {
                LOG.info("Folder already exists, ignoring as per configuration");
                return "{\"status\":\"ignored\"}";
            }
            throw e;
        }
    }
    
    /**
     * Execute delete file or folder operation
     */
    private String executeDelete(Exchange exchange, DropboxClient client) throws Exception {
        String path = getParameter(exchange, "path", endpoint.getFilePath());
        
        validateRequired(path, "path");
        
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        
        LOG.debug("Deleting: {}", path);
        
        return client.executeApiCall(DropboxConstants.API_DELETE, params);
    }
    
    /**
     * Execute get file URL operation
     */
    private String executeGetFileUrl(Exchange exchange, DropboxClient client) throws Exception {
        String filePath = getParameter(exchange, "filePath", endpoint.getFilePath());
        
        validateRequired(filePath, "filePath");
        
        Map<String, Object> params = new HashMap<>();
        params.put("path", filePath);
        
        LOG.debug("Getting temporary link for: {}", filePath);
        
        return client.executeApiCall(DropboxConstants.API_GET_TEMPORARY_LINK, params);
    }
    
    /**
     * Execute get metadata operation
     */
    private String executeGetMetadata(Exchange exchange, DropboxClient client) throws Exception {
        String path = getParameter(exchange, "path", endpoint.getFilePath());
        boolean includeDeleted = getBooleanParameter(exchange, DropboxConstants.HEADER_INCLUDE_DELETED, 
                                                      endpoint.isIncludeDeleted());
        
        validateRequired(path, "path");
        
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("include_deleted", includeDeleted);
        
        LOG.debug("Getting metadata for: {}", path);
        
        return client.executeApiCall(DropboxConstants.API_GET_METADATA, params);
    }
    
    /**
     * Execute get storage statistics operation
     */
    private String executeGetStorageStats(Exchange exchange, DropboxClient client) throws Exception {
        LOG.debug("Getting storage statistics");
        
        return client.executeApiCall(DropboxConstants.API_GET_SPACE_USAGE, null);
    }
    
    /**
     * Execute list folder operation
     */
    private String executeListFolder(Exchange exchange, DropboxClient client) throws Exception {
        String folderPath = getParameter(exchange, "folderPath", endpoint.getFolderPath());
        boolean recursive = getBooleanParameter(exchange, DropboxConstants.HEADER_RECURSIVE, endpoint.isRecursive());
        int limit = getIntParameter(exchange, DropboxConstants.HEADER_LIMIT, endpoint.getLimit());
        
        // Folder path can be empty for root
        if (folderPath == null) {
            folderPath = "";
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("path", folderPath);
        params.put("recursive", recursive);
        params.put("limit", Math.min(limit, 2000)); // Max 2000 as per Dropbox API
        
        LOG.debug("Listing folder: {} (recursive: {})", folderPath, recursive);
        
        return client.executeApiCall(DropboxConstants.API_LIST_FOLDER, params);
    }
    
    /**
     * Execute list revisions operation
     */
    private String executeListRevisions(Exchange exchange, DropboxClient client) throws Exception {
        String path = getParameter(exchange, "path", endpoint.getFilePath());
        String mode = getParameter(exchange, "mode", endpoint.getMode());
        int limit = getIntParameter(exchange, DropboxConstants.HEADER_LIMIT, endpoint.getLimit());
        
        validateRequired(path, "path");
        
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("mode", mode);
        params.put("limit", Math.min(limit, 100)); // Max 100 as per Dropbox API
        
        LOG.debug("Listing revisions for: {}", path);
        
        return client.executeApiCall(DropboxConstants.API_LIST_REVISIONS, params);
    }
    
    /**
     * Execute move file or folder operation
     */
    private String executeMove(Exchange exchange, DropboxClient client) throws Exception {
        String sourcePath = getParameter(exchange, "sourcePath", endpoint.getSourcePath());
        String destinationPath = getParameter(exchange, "destinationPath", endpoint.getDestinationPath());
        String handling = getHandlingExistingFiles(exchange);
        
        validateRequired(sourcePath, "sourcePath");
        validateRequired(destinationPath, "destinationPath");
        
        Map<String, Object> params = new HashMap<>();
        params.put("from_path", sourcePath);
        params.put("to_path", destinationPath);
        params.put("autorename", DropboxConstants.HANDLING_AUTO_RENAME.equalsIgnoreCase(handling));
        
        LOG.debug("Moving from {} to {}", sourcePath, destinationPath);
        
        return client.executeApiCall(DropboxConstants.API_MOVE, params);
    }
    
    /**
     * Execute update metadata operation
     */
    private String executeUpdateMetadata(Exchange exchange, DropboxClient client) throws Exception {
        String path = getParameter(exchange, "path", endpoint.getFilePath());
        String templateId = getParameter(exchange, "templateId", endpoint.getTemplateId());
        
        validateRequired(path, "path");
        validateRequired(templateId, "templateId");
        
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("property_groups", buildPropertyGroups(exchange));
        
        LOG.debug("Updating metadata for: {}", path);
        
        return client.executeApiCall(DropboxConstants.API_PROPERTIES_UPDATE, params);
    }
    
    /**
     * Execute search operation
     */
    private String executeSearch(Exchange exchange, DropboxClient client) throws Exception {
        String query = getParameter(exchange, "query", endpoint.getQuery());
        String searchPath = getParameter(exchange, "searchPath", endpoint.getSearchPath());
        int maxResults = getIntParameter(exchange, "maxResults", endpoint.getMaxResults());
        String orderBy = getParameter(exchange, DropboxConstants.HEADER_ORDER_BY, endpoint.getOrderBy());
        
        validateRequired(query, "query");
        
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> options = new HashMap<>();
        
        params.put("query", query);
        if (searchPath != null && !searchPath.isEmpty()) {
            params.put("options", options);
            options.put("path", searchPath);
        }
        options.put("max_results", Math.min(maxResults, 1000));
        
        if (DropboxConstants.ORDER_BY_MODIFIED_TIME.equalsIgnoreCase(orderBy)) {
            options.put("order_by", "last_modified_time");
        } else {
            options.put("order_by", "relevance");
        }
        
        LOG.debug("Searching for: {}", query);
        
        return client.executeApiCall(DropboxConstants.API_SEARCH, params);
    }
    
    /**
     * Execute upload file operation
     */
    private String executeUpload(Exchange exchange, DropboxClient client) throws Exception {
        String filePath = getParameter(exchange, "filePath", endpoint.getFilePath());
        String handling = getHandlingExistingFiles(exchange);
        boolean mute = getBooleanParameter(exchange, DropboxConstants.HEADER_MUTE, endpoint.isMute());
        
        validateRequired(filePath, "filePath");
        
        // Get content from exchange body
        byte[] content = exchange.getMessage().getBody(byte[].class);
        if (content == null) {
            throw new IllegalArgumentException("Message body is required for upload operation");
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("path", filePath);
        params.put("mode", getUploadMode(handling));
        params.put("autorename", DropboxConstants.HANDLING_AUTO_RENAME.equalsIgnoreCase(handling));
        params.put("mute", mute);
        
        LOG.debug("Uploading file to: {} ({} bytes)", filePath, content.length);
        
        try {
            return client.executeUpload(DropboxConstants.API_UPLOAD, content, params);
        } catch (Exception e) {
            if (DropboxConstants.HANDLING_IGNORE.equalsIgnoreCase(handling) && e.getMessage().contains("conflict")) {
                LOG.info("File already exists, ignoring as per configuration");
                return "{\"status\":\"ignored\"}";
            }
            throw e;
        }
    }
    
    // Helper methods
    
    private String getAccessToken() throws Exception {
        // In a real implementation, this would retrieve the OAuth2 token from SAP CPI Security Material
        // For now, we'll use the credential alias as a placeholder
        String credentialAlias = endpoint.getCredentialAlias();
        if (credentialAlias == null || credentialAlias.isEmpty()) {
            throw new IllegalArgumentException(DropboxConstants.ERROR_MISSING_CREDENTIAL);
        }
        
        // TODO: Implement actual OAuth2 token retrieval from Security Material
        // This is a placeholder that would be replaced with actual SAP CPI integration
        return credentialAlias;
    }
    
    private int getTimeout(Exchange exchange) {
        Integer headerTimeout = exchange.getMessage().getHeader(DropboxConstants.HEADER_TIMEOUT, Integer.class);
        return headerTimeout != null ? headerTimeout : endpoint.getTimeout();
    }
    
    private String getResponseFormat(Exchange exchange) {
        String format = exchange.getMessage().getHeader("responseFormat", String.class);
        return format != null ? format : endpoint.getResponseFormat();
    }
    
    private String getHandlingExistingFiles(Exchange exchange) {
        String handling = exchange.getMessage().getHeader(DropboxConstants.HEADER_HANDLING, String.class);
        if (handling == null) {
            handling = exchange.getMessage().getHeader(DropboxConstants.HEADER_AFTER_PROC, String.class);
        }
        return handling != null ? handling : endpoint.getHandlingExistingFiles();
    }
    
    private String getParameter(Exchange exchange, String headerName, String defaultValue) {
        String value = exchange.getMessage().getHeader(headerName, String.class);
        return value != null ? value : defaultValue;
    }
    
    private boolean getBooleanParameter(Exchange exchange, String headerName, boolean defaultValue) {
        String value = exchange.getMessage().getHeader(headerName, String.class);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    private int getIntParameter(Exchange exchange, String headerName, int defaultValue) {
        Integer value = exchange.getMessage().getHeader(headerName, Integer.class);
        return value != null ? value : defaultValue;
    }
    
    private void validateRequired(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " is required");
        }
    }
    
    private String getUploadMode(String handling) {
        if (DropboxConstants.HANDLING_OVERWRITE.equalsIgnoreCase(handling)) {
            return "overwrite";
        }
        return "add";
    }
    
    private Object buildPropertyGroups(Exchange exchange) {
        // Build property groups for metadata update
        // This would be implemented based on the actual requirements
        Map<String, Object> propertyGroup = new HashMap<>();
        propertyGroup.put("template_id", endpoint.getTemplateId());
        
        // Add fields from endpoint configuration
        if (endpoint.getAddOrUpdateFields() != null) {
            // Parse and add fields
        }
        
        return new Object[]{propertyGroup};
    }
}

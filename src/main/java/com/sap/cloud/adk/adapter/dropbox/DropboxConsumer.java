package com.sap.cloud.adk.adapter.dropbox;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.ScheduledPollConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Dropbox Consumer for Sender operations
 * Handles polling and downloading files from Dropbox
 */
public class DropboxConsumer extends ScheduledPollConsumer {
    
    private static final Logger LOG = LoggerFactory.getLogger(DropboxConsumer.class);
    
    private final DropboxEndpoint endpoint;
    private final Set<String> processedFiles;
    
    public DropboxConsumer(DropboxEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.processedFiles = new HashSet<>();
        
        // Set polling interval
        setDelay(endpoint.getPollingInterval());
        setTimeUnit(TimeUnit.MILLISECONDS);
    }
    
    @Override
    protected int poll() throws Exception {
        String operation = endpoint.getOperation();
        LOG.debug("Polling Dropbox for operation: {}", operation);
        
        // Get access token
        String accessToken = getAccessToken();
        int timeout = endpoint.getTimeout();
        
        // Create Dropbox client
        DropboxClient client = new DropboxClient(accessToken, timeout);
        
        int messagesPolled = 0;
        
        switch (operation) {
            case DropboxConstants.OPERATION_DOWNLOAD_FILE:
                messagesPolled = pollDownloadFile(client);
                break;
            case DropboxConstants.OPERATION_DOWNLOAD_ARCHIVE:
                messagesPolled = pollDownloadArchive(client);
                break;
            default:
                throw new IllegalArgumentException("Invalid sender operation: " + operation);
        }
        
        return messagesPolled;
    }
    
    /**
     * Poll and download files from Dropbox
     */
    private int pollDownloadFile(DropboxClient client) throws Exception {
        String filePath = endpoint.getFilePath();
        
        if (filePath == null || filePath.isEmpty()) {
            LOG.warn("File path not configured for download operation");
            return 0;
        }
        
        // Check if file should be processed (idempotent check)
        if (shouldSkipFile(filePath)) {
            LOG.debug("Skipping already processed file: {}", filePath);
            return 0;
        }
        
        try {
            // Download file
            Map<String, Object> params = new HashMap<>();
            params.put("path", filePath);
            
            LOG.info("Downloading file: {}", filePath);
            DropboxClient.DownloadResult result = client.executeDownload(DropboxConstants.API_DOWNLOAD, params);
            
            // Create exchange and process
            Exchange exchange = createExchange(false);
            exchange.getMessage().setBody(result.getContent());
            exchange.getMessage().setHeader("DropboxMetadata", result.getMetadata());
            exchange.getMessage().setHeader("DropboxFilePath", filePath);
            
            // Process the exchange
            getProcessor().process(exchange);
            
            // Post-processing
            performPostProcessing(client, filePath, exchange);
            
            // Mark as processed if using idempotent repository
            if (DropboxConstants.POST_PROC_IDEMPOTENT.equalsIgnoreCase(endpoint.getPostProcessing())) {
                markAsProcessed(filePath);
            }
            
            LOG.info("Successfully processed file: {}", filePath);
            return 1;
            
        } catch (Exception e) {
            LOG.error("Error downloading file: " + filePath, e);
            throw e;
        }
    }
    
    /**
     * Poll and download archive (zip) from Dropbox
     */
    private int pollDownloadArchive(DropboxClient client) throws Exception {
        String folderPath = endpoint.getFolderPath();
        
        if (folderPath == null || folderPath.isEmpty()) {
            LOG.warn("Folder path not configured for download archive operation");
            return 0;
        }
        
        try {
            // Download folder as zip
            Map<String, Object> params = new HashMap<>();
            params.put("path", folderPath);
            
            LOG.info("Downloading archive for folder: {}", folderPath);
            DropboxClient.DownloadResult result = client.executeDownload(DropboxConstants.API_DOWNLOAD_ZIP, params);
            
            // Create exchange and process
            Exchange exchange = createExchange(false);
            exchange.getMessage().setBody(result.getContent());
            exchange.getMessage().setHeader("DropboxMetadata", result.getMetadata());
            exchange.getMessage().setHeader("DropboxFolderPath", folderPath);
            exchange.getMessage().setHeader("ContentType", "application/zip");
            
            // Process the exchange
            getProcessor().process(exchange);
            
            LOG.info("Successfully processed archive for folder: {}", folderPath);
            return 1;
            
        } catch (Exception e) {
            LOG.error("Error downloading archive for folder: " + folderPath, e);
            throw e;
        }
    }
    
    /**
     * Perform post-processing operations after successful download
     */
    private void performPostProcessing(DropboxClient client, String filePath, Exchange exchange) {
        String postProcessing = endpoint.getPostProcessing();
        boolean raiseException = endpoint.isRaiseExceptionOnPostProcessingFailure();
        
        LOG.debug("Performing post-processing: {} for file: {}", postProcessing, filePath);
        
        try {
            switch (postProcessing) {
                case DropboxConstants.POST_PROC_DELETE:
                    deleteFile(client, filePath, exchange);
                    break;
                    
                case DropboxConstants.POST_PROC_MOVE:
                    moveFile(client, filePath, exchange);
                    break;
                    
                case DropboxConstants.POST_PROC_KEEP:
                    // Do nothing, just keep the file
                    LOG.debug("Keeping file as per configuration: {}", filePath);
                    exchange.getMessage().setHeader(DropboxConstants.HEADER_KEEPFILE_RESPONSE, "success");
                    break;
                    
                case DropboxConstants.POST_PROC_IDEMPOTENT:
                    // File will be marked as processed in the idempotent repository
                    LOG.debug("Marking file as processed in idempotent repository: {}", filePath);
                    exchange.getMessage().setHeader(DropboxConstants.HEADER_KEEPFILE_RESPONSE, "success");
                    break;
                    
                default:
                    LOG.warn("Unknown post-processing option: {}", postProcessing);
            }
        } catch (Exception e) {
            LOG.error("Post-processing failed for file: " + filePath, e);
            
            if (raiseException) {
                throw new RuntimeException("Post-processing failed: " + e.getMessage(), e);
            } else {
                // Log error but don't fail the main processing
                exchange.getMessage().setHeader("PostProcessingError", e.getMessage());
            }
        }
    }
    
    /**
     * Delete file from Dropbox
     */
    private void deleteFile(DropboxClient client, String filePath, Exchange exchange) throws Exception {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("path", filePath);
            
            String response = client.executeApiCall(DropboxConstants.API_DELETE, params);
            exchange.getMessage().setHeader(DropboxConstants.HEADER_DELETE_RESPONSE, response);
            
            LOG.info("File deleted successfully: {}", filePath);
        } catch (Exception e) {
            exchange.getMessage().setHeader(DropboxConstants.HEADER_DELETE_RESPONSE, "failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Move file to archive directory
     */
    private void moveFile(DropboxClient client, String filePath, Exchange exchange) throws Exception {
        String archiveDirectory = endpoint.getArchiveDirectory();
        
        if (archiveDirectory == null || archiveDirectory.isEmpty()) {
            throw new IllegalArgumentException("Archive directory must be specified for move post-processing");
        }
        
        try {
            // Extract filename from path
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            String destinationPath = archiveDirectory + "/" + fileName;
            
            Map<String, Object> params = new HashMap<>();
            params.put("from_path", filePath);
            params.put("to_path", destinationPath);
            params.put("autorename", true);
            
            String response = client.executeApiCall(DropboxConstants.API_MOVE, params);
            exchange.getMessage().setHeader(DropboxConstants.HEADER_ARCHIVE_RESPONSE, response);
            
            LOG.info("File moved successfully from {} to {}", filePath, destinationPath);
        } catch (Exception e) {
            exchange.getMessage().setHeader(DropboxConstants.HEADER_ARCHIVE_RESPONSE, "failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Check if file should be skipped based on idempotent repository
     */
    private boolean shouldSkipFile(String filePath) {
        if (!DropboxConstants.POST_PROC_IDEMPOTENT.equalsIgnoreCase(endpoint.getPostProcessing())) {
            return false;
        }
        
        // Check if file was already processed
        return processedFiles.contains(filePath);
    }
    
    /**
     * Mark file as processed in idempotent repository
     */
    private void markAsProcessed(String filePath) {
        processedFiles.add(filePath);
        
        // In a real implementation, this would persist to a database
        // For now, we keep it in memory
        LOG.debug("File marked as processed: {}", filePath);
        
        // Clean up old entries based on persist duration
        // This is a simplified implementation
        if (processedFiles.size() > 10000) {
            LOG.warn("Idempotent repository size exceeded 10000 entries, clearing old entries");
            processedFiles.clear();
        }
    }
    
    /**
     * Get access token from credential alias
     */
    private String getAccessToken() throws Exception {
        String credentialAlias = endpoint.getCredentialAlias();
        if (credentialAlias == null || credentialAlias.isEmpty()) {
            throw new IllegalArgumentException(DropboxConstants.ERROR_MISSING_CREDENTIAL);
        }
        
        // TODO: Implement actual OAuth2 token retrieval from Security Material
        // This is a placeholder that would be replaced with actual SAP CPI integration
        return credentialAlias;
    }
    
    @Override
    protected void doStart() throws Exception {
        super.doStart();
        LOG.info("Dropbox Consumer started for operation: {}", endpoint.getOperation());
    }
    
    @Override
    protected void doStop() throws Exception {
        super.doStop();
        LOG.info("Dropbox Consumer stopped for operation: {}", endpoint.getOperation());
    }
}

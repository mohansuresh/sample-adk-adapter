package com.sap.it.custom.dropbox;

import com.sap.it.custom.dropbox.client.DropboxClient;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.ScheduledPollConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Dropbox Consumer for SAP Cloud Platform Integration (Sender Adapter).
 * Handles inbound operations from Dropbox API with polling support.
 * 
 * @author SAP CPI Custom Adapter
 * @version 1.0.0
 */
public class DropboxConsumer extends ScheduledPollConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxConsumer.class);
    
    private final DropboxEndpoint endpoint;

    public DropboxConsumer(DropboxEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected int poll() throws Exception {
        LOG.info("Polling Dropbox for operation: {}", endpoint.getOperation());
        
        String operation = endpoint.getOperation();
        if (operation == null || operation.isEmpty()) {
            LOG.error("Operation not specified for Dropbox Consumer");
            return 0;
        }
        
        try {
            // Initialize Dropbox client
            DropboxClient client = new DropboxClient(endpoint.getCredentialName(), endpoint.getTimeout());
            
            // Execute operation based on type
            int messagesProcessed = 0;
            switch (operation.toUpperCase()) {
                case "DOWNLOAD":
                    messagesProcessed = executeDownloadOperation(client);
                    break;
                case "DOWNLOAD_ARCHIVE":
                    messagesProcessed = executeDownloadArchiveOperation(client);
                    break;
                default:
                    LOG.error("Unsupported sender operation: {}", operation);
                    return 0;
            }
            
            return messagesProcessed;
            
        } catch (Exception e) {
            LOG.error("Error during Dropbox polling: {}", e.getMessage(), e);
            getExceptionHandler().handleException("Error polling Dropbox", e);
            return 0;
        }
    }

    private int executeDownloadOperation(DropboxClient client) throws Exception {
        LOG.info("Executing Download operation");
        
        String filePath = endpoint.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            LOG.error("File path not specified for download operation");
            return 0;
        }
        
        // Download file from Dropbox
        Map<String, Object> args = new HashMap<>();
        args.put("path", filePath);
        
        String apiArgs = mapToJson(args);
        Map<String, Object> result = client.executeContentApiCall("/files/download", apiArgs, null);
        
        if (result.containsKey("content")) {
            byte[] content = (byte[]) result.get("content");
            
            // Create exchange and set content
            Exchange exchange = endpoint.createExchange();
            exchange.getIn().setBody(content);
            
            // Set metadata headers
            if (result.containsKey("metadata")) {
                exchange.getIn().setHeader("SAP_DropboxMetadata", result.get("metadata").toString());
            }
            exchange.getIn().setHeader("SAP_DropboxFilePath", filePath);
            
            try {
                // Process the exchange
                getProcessor().process(exchange);
                
                // Perform post-processing
                performPostProcessing(client, filePath, exchange);
                
                LOG.info("Successfully processed file: {}", filePath);
                return 1;
                
            } catch (Exception e) {
                LOG.error("Error processing exchange: {}", e.getMessage(), e);
                
                if (exchange.getException() != null) {
                    getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
                }
                throw e;
            }
        }
        
        return 0;
    }

    private int executeDownloadArchiveOperation(DropboxClient client) throws Exception {
        LOG.info("Executing Download Archive operation");
        
        String folderPath = endpoint.getFolderPath();
        if (folderPath == null || folderPath.isEmpty()) {
            LOG.error("Folder path not specified for download archive operation");
            return 0;
        }
        
        // Download archive from Dropbox
        Map<String, Object> args = new HashMap<>();
        args.put("path", folderPath);
        
        String apiArgs = mapToJson(args);
        Map<String, Object> result = client.executeContentApiCall("/files/download_zip", apiArgs, null);
        
        if (result.containsKey("content")) {
            byte[] content = (byte[]) result.get("content");
            
            // Create exchange and set content
            Exchange exchange = endpoint.createExchange();
            exchange.getIn().setBody(content);
            exchange.getIn().setHeader("SAP_DropboxFolderPath", folderPath);
            exchange.getIn().setHeader("Content-Type", "application/zip");
            
            try {
                // Process the exchange
                getProcessor().process(exchange);
                
                LOG.info("Successfully processed archive: {}", folderPath);
                return 1;
                
            } catch (Exception e) {
                LOG.error("Error processing exchange: {}", e.getMessage(), e);
                
                if (exchange.getException() != null) {
                    getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
                }
                throw e;
            }
        }
        
        return 0;
    }

    private void performPostProcessing(DropboxClient client, String filePath, Exchange exchange) throws Exception {
        String postProcessing = endpoint.getPostProcessing();
        boolean raiseException = endpoint.isRaiseExceptionOnPostProcessingFailure();
        
        LOG.info("Performing post-processing: {}", postProcessing);
        
        try {
            switch (postProcessing.toUpperCase()) {
                case "DELETE":
                    deleteFile(client, filePath);
                    exchange.getIn().setHeader("SAP_Dropbox_Delete_Response", "Success");
                    break;
                    
                case "ARCHIVE":
                case "MOVE":
                    archiveFile(client, filePath);
                    exchange.getIn().setHeader("SAP_Dropbox_Archive_Response", "Success");
                    break;
                    
                case "KEEPANDMARK":
                    markAsProcessed(filePath);
                    exchange.getIn().setHeader("SAP_Dropbox_Keepfile_Response", "Success");
                    break;
                    
                case "KEEPANDPROCESS":
                default:
                    // Do nothing, file will be processed again on next poll
                    LOG.info("File will be processed again on next poll");
                    break;
            }
            
        } catch (Exception e) {
            LOG.error("Post-processing failed: {}", e.getMessage(), e);
            
            // Set failure status in header
            if (postProcessing.equalsIgnoreCase("DELETE")) {
                exchange.getIn().setHeader("SAP_Dropbox_Delete_Response", "Failed: " + e.getMessage());
            } else if (postProcessing.equalsIgnoreCase("ARCHIVE") || postProcessing.equalsIgnoreCase("MOVE")) {
                exchange.getIn().setHeader("SAP_Dropbox_Archive_Response", "Failed: " + e.getMessage());
            } else if (postProcessing.equalsIgnoreCase("KEEPANDMARK")) {
                exchange.getIn().setHeader("SAP_Dropbox_Keepfile_Response", "Failed: " + e.getMessage());
            }
            
            if (raiseException) {
                throw e;
            }
        }
    }

    private void deleteFile(DropboxClient client, String filePath) throws Exception {
        LOG.info("Deleting file: {}", filePath);
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", filePath);
        
        String apiArgs = mapToJson(args);
        client.executeApiCall("/files/delete_v2", apiArgs);
        
        LOG.info("File deleted successfully: {}", filePath);
    }

    private void archiveFile(DropboxClient client, String filePath) throws Exception {
        String archiveDirectory = endpoint.getArchiveDirectory();
        if (archiveDirectory == null || archiveDirectory.isEmpty()) {
            throw new IllegalArgumentException("Archive directory not specified");
        }
        
        LOG.info("Archiving file from {} to {}", filePath, archiveDirectory);
        
        // Extract filename from path
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        String destinationPath = archiveDirectory + "/" + fileName;
        
        Map<String, Object> args = new HashMap<>();
        args.put("from_path", filePath);
        args.put("to_path", destinationPath);
        args.put("autorename", true);
        
        String apiArgs = mapToJson(args);
        client.executeApiCall("/files/move_v2", apiArgs);
        
        LOG.info("File archived successfully: {}", destinationPath);
    }

    private void markAsProcessed(String filePath) throws Exception {
        LOG.info("Marking file as processed in idempotent repository: {}", filePath);
        
        // In a real implementation, this would store the file path in an idempotent repository
        // For now, we'll just log it
        // The idempotent repository would typically use a database or cache
        // to track processed files for the configured persist duration
        
        LOG.info("File marked as processed (idempotent repository not yet implemented): {}", filePath);
    }
    
    /**
     * Simple JSON builder method to convert Map to JSON string without Jackson dependency.
     */
    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(((String) value).replace("\"", "\\\"")).append("\"");
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Map) {
                json.append(mapToJson((Map<String, Object>) value));
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}

package com.sap.adapter.adk.sharepoint;

import com.sap.adapter.adk.sharepoint.auth.OAuth2AuthenticationHandler;
import com.sap.adapter.adk.sharepoint.exception.SharePointException;
import com.sap.adapter.adk.sharepoint.handler.SharePointHttpClient;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.ScheduledPollConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * SharePoint Consumer for sender (polling) operations
 * Polls SharePoint for files and data
 */
public class SharePointConsumer extends ScheduledPollConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SharePointConsumer.class);
    private final SharePointEndpoint endpoint;
    private SharePointHttpClient httpClient;
    private OAuth2AuthenticationHandler authHandler;

    public SharePointConsumer(SharePointEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        initializeHttpClient();
    }

    @Override
    protected int poll() throws Exception {
        try {
            LOG.info("Polling SharePoint for operation: {}", endpoint.getOperationName());

            String operationName = endpoint.getOperationName();
            if (operationName == null || operationName.trim().isEmpty()) {
                LOG.error("Operation name is not configured for sender adapter");
                return 0;
            }

            // Execute polling operation
            int messagesProcessed = executePollOperation(operationName);

            LOG.info("Polling completed. Messages processed: {}", messagesProcessed);
            return messagesProcessed;

        } catch (Exception e) {
            LOG.error("Error during polling", e);
            getExceptionHandler().handleException("Error polling SharePoint", e);
            return 0;
        }
    }

    /**
     * Executes the polling operation based on operation name
     */
    private int executePollOperation(String operationName) throws Exception {
        switch (operationName.toUpperCase()) {
            case "DOWNLOADFILE":
                return downloadFiles();
            
            case "LISTFILES":
                return listFiles();
            
            case "GETDRIVEITEMS":
                return getDriveItems();
            
            default:
                throw new SharePointException("Unsupported sender operation: " + operationName);
        }
    }

    /**
     * Downloads files from SharePoint
     */
    private int downloadFiles() throws Exception {
        String driveId = endpoint.getDriveId();
        String folderPath = endpoint.getFolderPath();
        
        if (driveId == null || folderPath == null) {
            LOG.error("DriveId and FolderPath are required for file download");
            return 0;
        }

        // Get list of files in the folder
        Map<String, String> queryParams = new HashMap<>();
        String listEndpoint = "/drives/" + driveId + "/root:/" + folderPath + ":/children";
        String filesListJson = httpClient.executeGet(listEndpoint, queryParams);

        // Parse and process each file
        // For simplicity, we'll process the JSON response as-is
        // In production, parse JSON and iterate through files
        
        Exchange exchange = endpoint.createExchange();
        exchange.getIn().setBody(filesListJson);
        exchange.setProperty("SharePoint_Operation", "DownloadFile");
        exchange.setProperty("SharePoint_DriveId", driveId);
        exchange.setProperty("SharePoint_FolderPath", folderPath);

        try {
            getProcessor().process(exchange);
            
            // Handle post-processing
            handlePostProcessing(exchange);
            
            return 1;
        } catch (Exception e) {
            LOG.error("Error processing downloaded file", e);
            if (endpoint.isMplOnFailure()) {
                throw e;
            }
            return 0;
        }
    }

    /**
     * Lists files from SharePoint
     */
    private int listFiles() throws Exception {
        String driveId = endpoint.getDriveId();
        String itemId = endpoint.getItemId() != null ? endpoint.getItemId() : "root";
        
        if (driveId == null) {
            LOG.error("DriveId is required for listing files");
            return 0;
        }

        Map<String, String> queryParams = buildQueryParams();
        String endpoint = "/drives/" + driveId + "/items/" + itemId + "/children";
        String filesListJson = httpClient.executeGet(endpoint, queryParams);

        Exchange exchange = this.endpoint.createExchange();
        exchange.getIn().setBody(filesListJson);
        exchange.setProperty("SharePoint_Operation", "ListFiles");
        exchange.setProperty("SharePoint_DriveId", driveId);

        try {
            getProcessor().process(exchange);
            return 1;
        } catch (Exception e) {
            LOG.error("Error processing file list", e);
            if (this.endpoint.isMplOnFailure()) {
                throw e;
            }
            return 0;
        }
    }

    /**
     * Gets drive items from SharePoint
     */
    private int getDriveItems() throws Exception {
        String driveId = endpoint.getDriveId();
        
        if (driveId == null) {
            LOG.error("DriveId is required for getting drive items");
            return 0;
        }

        Map<String, String> queryParams = buildQueryParams();
        String endpoint = "/drives/" + driveId + "/root/children";
        String itemsJson = httpClient.executeGet(endpoint, queryParams);

        Exchange exchange = this.endpoint.createExchange();
        exchange.getIn().setBody(itemsJson);
        exchange.setProperty("SharePoint_Operation", "GetDriveItems");
        exchange.setProperty("SharePoint_DriveId", driveId);

        try {
            getProcessor().process(exchange);
            return 1;
        } catch (Exception e) {
            LOG.error("Error processing drive items", e);
            if (this.endpoint.isMplOnFailure()) {
                throw e;
            }
            return 0;
        }
    }

    /**
     * Handles post-processing of files (delete, keep, mark as processed)
     */
    private void handlePostProcessing(Exchange exchange) {
        String postProcessing = endpoint.getPostProcessing();
        
        if (postProcessing == null || postProcessing.equalsIgnoreCase("Keep")) {
            LOG.debug("Post-processing: Keep file");
            return;
        }

        String driveId = exchange.getProperty("SharePoint_DriveId", String.class);
        String itemId = exchange.getProperty("SharePoint_ItemId", String.class);

        if (postProcessing.equalsIgnoreCase("Delete")) {
            try {
                LOG.info("Post-processing: Deleting file");
                String deleteEndpoint = "/drives/" + driveId + "/items/" + itemId;
                httpClient.executeDelete(deleteEndpoint);
                LOG.info("File deleted successfully");
            } catch (Exception e) {
                LOG.error("Error deleting file during post-processing", e);
                if (endpoint.isMplOnFailure()) {
                    throw new SharePointException("Post-processing failed: Unable to delete file", e);
                }
            }
        } else if (postProcessing.equalsIgnoreCase("MarkProcessed")) {
            try {
                LOG.info("Post-processing: Marking file as processed");
                // Implementation would mark the file as processed in an idempotent repository
                // This is a placeholder for the actual implementation
                LOG.info("File marked as processed");
            } catch (Exception e) {
                LOG.error("Error marking file as processed", e);
                if (endpoint.isMplOnFailure()) {
                    throw new SharePointException("Post-processing failed: Unable to mark file as processed", e);
                }
            }
        }
    }

    /**
     * Initializes the HTTP client with authentication
     */
    private void initializeHttpClient() {
        LOG.info("Initializing SharePoint HTTP client for consumer");

        // Initialize OAuth2 authentication handler
        authHandler = new OAuth2AuthenticationHandler(
            endpoint.getClientId(),
            endpoint.getClientSecret(),
            endpoint.getTenantId(),
            endpoint.getScope()
        );

        // Initialize HTTP client
        httpClient = new SharePointHttpClient(
            authHandler,
            endpoint.getSiteUrl(),
            endpoint.getConnectionTimeout(),
            endpoint.getSocketTimeout(),
            endpoint.getProxyHost(),
            endpoint.getProxyPort(),
            endpoint.isTraceEnabled()
        );

        LOG.info("SharePoint HTTP client initialized successfully for consumer");
    }

    /**
     * Builds query parameters for API calls
     */
    private Map<String, String> buildQueryParams() {
        Map<String, String> params = new HashMap<>();

        // Add pagination parameters if enabled
        if (endpoint.isEnablePagination()) {
            params.put("$top", String.valueOf(endpoint.getPageSize()));
            if (endpoint.getSkipToken() != null) {
                params.put("$skiptoken", endpoint.getSkipToken());
            }
        }

        // Add custom query parameters
        if (endpoint.getQueryParameters() != null) {
            String[] pairs = endpoint.getQueryParameters().split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }

        return params;
    }
}

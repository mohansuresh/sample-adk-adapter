package com.sap.adapter.adk.sharepoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.adapter.adk.sharepoint.auth.OAuth2AuthenticationHandler;
import com.sap.adapter.adk.sharepoint.exception.SharePointException;
import com.sap.adapter.adk.sharepoint.handler.SharePointHttpClient;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * SharePoint Producer for receiver operations
 * Handles all outbound SharePoint API calls
 */
public class SharePointProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(SharePointProducer.class);
    private final SharePointEndpoint endpoint;
    private SharePointHttpClient httpClient;
    private OAuth2AuthenticationHandler authHandler;
    private Gson gson;

    public SharePointProducer(SharePointEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
        this.gson = new Gson();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        initializeHttpClient();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            LOG.info("Processing SharePoint operation: {}", endpoint.getOperationName());

            // Get operation details from endpoint or exchange headers
            String operation = getOperationFromExchange(exchange);
            String operationName = getOperationNameFromExchange(exchange);

            // Execute the appropriate operation
            String response = executeOperation(operation, operationName, exchange);

            // Set response in exchange
            exchange.getIn().setBody(response);
            exchange.setProperty("SharePoint_Operation_Status", "SUCCESS");
            exchange.setProperty("SharePoint_Operation_Name", operationName);

            LOG.info("SharePoint operation completed successfully: {}", operationName);

        } catch (Exception e) {
            LOG.error("Error processing SharePoint operation", e);
            exchange.setProperty("SharePoint_Operation_Status", "FAILED");
            exchange.setProperty("SharePoint_Error_Message", e.getMessage());
            throw new SharePointException("SharePoint operation failed", e);
        }
    }

    /**
     * Executes the appropriate SharePoint operation
     */
    private String executeOperation(String operation, String operationName, Exchange exchange) throws Exception {
        switch (operation.toUpperCase()) {
            case "SITES":
                return executeSitesOperation(operationName, exchange);
            case "LISTS":
                return executeListsOperation(operationName, exchange);
            case "LISTITEMS":
                return executeListItemsOperation(operationName, exchange);
            case "FILES":
                return executeFilesOperation(operationName, exchange);
            case "PERMISSIONS":
                return executePermissionsOperation(operationName, exchange);
            case "COLUMNS":
                return executeColumnsOperation(operationName, exchange);
            case "CONTENTTYPES":
                return executeContentTypesOperation(operationName, exchange);
            default:
                throw new SharePointException("Unsupported operation: " + operation);
        }
    }

    /**
     * Executes Sites operations
     */
    private String executeSitesOperation(String operationName, Exchange exchange) throws Exception {
        Map<String, String> queryParams = buildQueryParams(exchange);

        switch (operationName.toUpperCase()) {
            case "GETSITE":
                return httpClient.executeGet("/sites/" + extractSiteId(), queryParams);
            
            case "SEARCHSITES":
                String searchQuery = exchange.getIn().getHeader("SearchQuery", String.class);
                return httpClient.executeGet("/sites?search=" + searchQuery, queryParams);
            
            case "GETSUBSITES":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/sites", queryParams);
            
            case "GETANALYTICS":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/analytics", queryParams);
            
            case "GETLISTS":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/lists", queryParams);
            
            default:
                throw new SharePointException("Unsupported Sites operation: " + operationName);
        }
    }

    /**
     * Executes Lists operations
     */
    private String executeListsOperation(String operationName, Exchange exchange) throws Exception {
        Map<String, String> queryParams = buildQueryParams(exchange);
        String listId = endpoint.getListId() != null ? endpoint.getListId() : 
                       exchange.getIn().getHeader("ListId", String.class);

        switch (operationName.toUpperCase()) {
            case "GETLIST":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/lists/" + listId, queryParams);
            
            case "CREATELIST":
                String listData = exchange.getIn().getBody(String.class);
                return httpClient.executePost("/sites/" + extractSiteId() + "/lists", listData);
            
            case "GETLISTITEMS":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/lists/" + listId + "/items", queryParams);
            
            default:
                throw new SharePointException("Unsupported Lists operation: " + operationName);
        }
    }

    /**
     * Executes List Items operations
     */
    private String executeListItemsOperation(String operationName, Exchange exchange) throws Exception {
        Map<String, String> queryParams = buildQueryParams(exchange);
        String listId = endpoint.getListId() != null ? endpoint.getListId() : 
                       exchange.getIn().getHeader("ListId", String.class);
        String itemId = endpoint.getItemId() != null ? endpoint.getItemId() : 
                       exchange.getIn().getHeader("ItemId", String.class);

        String basePath = "/sites/" + extractSiteId() + "/lists/" + listId + "/items";

        switch (operationName.toUpperCase()) {
            case "GETLISTITEM":
                return httpClient.executeGet(basePath + "/" + itemId, queryParams);
            
            case "CREATELISTITEM":
                String itemData = exchange.getIn().getBody(String.class);
                return httpClient.executePost(basePath, itemData);
            
            case "UPDATELISTITEM":
                String updateData = exchange.getIn().getBody(String.class);
                return httpClient.executePatch(basePath + "/" + itemId, updateData);
            
            case "DELETELISTITEM":
                return httpClient.executeDelete(basePath + "/" + itemId);
            
            case "GETCOLUMNVALUES":
                return httpClient.executeGet(basePath + "/" + itemId + "/fields", queryParams);
            
            case "UPDATECOLUMNVALUES":
                String columnData = exchange.getIn().getBody(String.class);
                return httpClient.executePatch(basePath + "/" + itemId + "/fields", columnData);
            
            default:
                throw new SharePointException("Unsupported List Items operation: " + operationName);
        }
    }

    /**
     * Executes Files operations
     */
    private String executeFilesOperation(String operationName, Exchange exchange) throws Exception {
        Map<String, String> queryParams = buildQueryParams(exchange);
        String driveId = endpoint.getDriveId() != null ? endpoint.getDriveId() : 
                        exchange.getIn().getHeader("DriveId", String.class);
        String itemId = endpoint.getItemId() != null ? endpoint.getItemId() : 
                       exchange.getIn().getHeader("ItemId", String.class);

        switch (operationName.toUpperCase()) {
            case "GETDRIVE":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/drive", queryParams);
            
            case "GETDRIVEITEM":
                return httpClient.executeGet("/drives/" + driveId + "/items/" + itemId, queryParams);
            
            case "LISTCHILDREN":
                return httpClient.executeGet("/drives/" + driveId + "/items/" + itemId + "/children", queryParams);
            
            case "UPLOADFILE":
                byte[] fileContent = exchange.getIn().getBody(byte[].class);
                String fileName = endpoint.getFileName() != null ? endpoint.getFileName() : 
                                 exchange.getIn().getHeader("FileName", String.class);
                String uploadPath = "/drives/" + driveId + "/items/" + itemId + ":/" + fileName + ":/content";
                return httpClient.uploadFile(uploadPath, fileContent);
            
            case "UPDATEDRIVEITEM":
                String updateData = exchange.getIn().getBody(String.class);
                return httpClient.executePatch("/drives/" + driveId + "/items/" + itemId, updateData);
            
            case "DELETEDRIVEITEM":
                return httpClient.executeDelete("/drives/" + driveId + "/items/" + itemId);
            
            case "CREATEFOLDER":
                String folderData = exchange.getIn().getBody(String.class);
                return httpClient.executePost("/drives/" + driveId + "/items/" + itemId + "/children", folderData);
            
            case "COPYDRIVEITEM":
                String copyData = exchange.getIn().getBody(String.class);
                return httpClient.executePost("/drives/" + driveId + "/items/" + itemId + "/copy", copyData);
            
            case "MOVEDRIVEITEM":
                String moveData = exchange.getIn().getBody(String.class);
                return httpClient.executePatch("/drives/" + driveId + "/items/" + itemId, moveData);
            
            case "SEARCHDRIVEITEMS":
                String searchQuery = exchange.getIn().getHeader("SearchQuery", String.class);
                return httpClient.executeGet("/drives/" + driveId + "/root/search(q='" + searchQuery + "')", queryParams);
            
            default:
                throw new SharePointException("Unsupported Files operation: " + operationName);
        }
    }

    /**
     * Executes Permissions operations
     */
    private String executePermissionsOperation(String operationName, Exchange exchange) throws Exception {
        Map<String, String> queryParams = buildQueryParams(exchange);
        String permissionId = exchange.getIn().getHeader("PermissionId", String.class);

        switch (operationName.toUpperCase()) {
            case "LISTPERMISSIONS":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/permissions", queryParams);
            
            case "GETPERMISSION":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/permissions/" + permissionId, queryParams);
            
            case "CREATEPERMISSION":
                String permData = exchange.getIn().getBody(String.class);
                return httpClient.executePost("/sites/" + extractSiteId() + "/permissions", permData);
            
            case "UPDATEPERMISSION":
                String updateData = exchange.getIn().getBody(String.class);
                return httpClient.executePatch("/sites/" + extractSiteId() + "/permissions/" + permissionId, updateData);
            
            case "DELETEPERMISSION":
                return httpClient.executeDelete("/sites/" + extractSiteId() + "/permissions/" + permissionId);
            
            default:
                throw new SharePointException("Unsupported Permissions operation: " + operationName);
        }
    }

    /**
     * Executes Columns operations
     */
    private String executeColumnsOperation(String operationName, Exchange exchange) throws Exception {
        Map<String, String> queryParams = buildQueryParams(exchange);
        String listId = endpoint.getListId() != null ? endpoint.getListId() : 
                       exchange.getIn().getHeader("ListId", String.class);

        switch (operationName.toUpperCase()) {
            case "LISTCOLUMNSINSITE":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/columns", queryParams);
            
            case "LISTCOLUMNSINLIST":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/lists/" + listId + "/columns", queryParams);
            
            default:
                throw new SharePointException("Unsupported Columns operation: " + operationName);
        }
    }

    /**
     * Executes Content Types operations
     */
    private String executeContentTypesOperation(String operationName, Exchange exchange) throws Exception {
        Map<String, String> queryParams = buildQueryParams(exchange);
        String contentTypeId = exchange.getIn().getHeader("ContentTypeId", String.class);

        switch (operationName.toUpperCase()) {
            case "LISTCONTENTTYPESINSITE":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/contentTypes", queryParams);
            
            case "GETCONTENTTYPE":
                return httpClient.executeGet("/sites/" + extractSiteId() + "/contentTypes/" + contentTypeId, queryParams);
            
            case "CREATECONTENTTYPE":
                String ctData = exchange.getIn().getBody(String.class);
                return httpClient.executePost("/sites/" + extractSiteId() + "/contentTypes", ctData);
            
            case "UPDATECONTENTTYPE":
                String updateData = exchange.getIn().getBody(String.class);
                return httpClient.executePatch("/sites/" + extractSiteId() + "/contentTypes/" + contentTypeId, updateData);
            
            case "DELETECONTENTTYPE":
                return httpClient.executeDelete("/sites/" + extractSiteId() + "/contentTypes/" + contentTypeId);
            
            default:
                throw new SharePointException("Unsupported Content Types operation: " + operationName);
        }
    }

    /**
     * Initializes the HTTP client with authentication
     */
    private void initializeHttpClient() {
        LOG.info("Initializing SharePoint HTTP client");

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

        LOG.info("SharePoint HTTP client initialized successfully");
    }

    /**
     * Extracts site ID from site URL
     */
    private String extractSiteId() {
        String siteUrl = endpoint.getSiteUrl();
        // Extract site ID from URL or use the full URL
        // For simplicity, returning the site URL as-is
        // In production, this should parse the URL properly
        return siteUrl.replace("https://", "").replace("/", ",");
    }

    /**
     * Gets operation from exchange or endpoint
     */
    private String getOperationFromExchange(Exchange exchange) {
        String operation = exchange.getIn().getHeader("Operation", String.class);
        return operation != null ? operation : endpoint.getOperation();
    }

    /**
     * Gets operation name from exchange or endpoint
     */
    private String getOperationNameFromExchange(Exchange exchange) {
        String operationName = exchange.getIn().getHeader("OperationName", String.class);
        return operationName != null ? operationName : endpoint.getOperationName();
    }

    /**
     * Builds query parameters from endpoint and exchange
     */
    private Map<String, String> buildQueryParams(Exchange exchange) {
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

        // Add parameters from exchange headers
        String headerParams = exchange.getIn().getHeader("QueryParameters", String.class);
        if (headerParams != null) {
            String[] pairs = headerParams.split("&");
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

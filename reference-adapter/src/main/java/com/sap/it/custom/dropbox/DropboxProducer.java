package com.sap.it.custom.dropbox;

import com.sap.it.custom.dropbox.client.DropboxClient;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Dropbox Producer for SAP Cloud Platform Integration (Receiver Adapter).
 * Handles outbound operations to Dropbox API.
 * 
 * @author SAP CPI Custom Adapter
 * @version 1.0.0
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
        LOG.info("Processing Dropbox Producer operation: {}", endpoint.getOperation());
        
        // Get dynamic properties from exchange
        String operation = getOperationFromExchange(exchange);
        
        // Initialize Dropbox client
        DropboxClient client = new DropboxClient(endpoint.getCredentialName(), getTimeoutFromExchange(exchange));
        
        // Execute operation based on type
        Map<String, Object> result;
        switch (operation.toUpperCase()) {
            case "COPY":
                result = executeCopyOperation(client, exchange);
                break;
            case "CREATE_FOLDER":
                result = executeCreateFolderOperation(client, exchange);
                break;
            case "DELETE":
                result = executeDeleteOperation(client, exchange);
                break;
            case "GET_FILE_URL":
                result = executeGetFileUrlOperation(client, exchange);
                break;
            case "GET_METADATA":
                result = executeGetMetadataOperation(client, exchange);
                break;
            case "GET_STORAGE_STATISTICS":
                result = executeGetStorageStatisticsOperation(client, exchange);
                break;
            case "LIST_FOLDER":
                result = executeListFolderOperation(client, exchange);
                break;
            case "LIST_REVISIONS":
                result = executeListRevisionsOperation(client, exchange);
                break;
            case "MOVE":
                result = executeMoveOperation(client, exchange);
                break;
            case "UPDATE_METADATA":
                result = executeUpdateMetadataOperation(client, exchange);
                break;
            case "SEARCH":
                result = executeSearchOperation(client, exchange);
                break;
            case "UPLOAD":
                result = executeUploadOperation(client, exchange);
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
        
        // Set response in exchange
        setResponseInExchange(exchange, result);
        
        LOG.info("Dropbox Producer operation completed successfully");
    }

    private String getOperationFromExchange(Exchange exchange) {
        String operation = exchange.getIn().getHeader("SAP_DropboxOperation", String.class);
        return operation != null ? operation : endpoint.getOperation();
    }

    private int getTimeoutFromExchange(Exchange exchange) {
        Integer timeout = exchange.getIn().getHeader("SAP_DropboxTimeout", Integer.class);
        return timeout != null ? timeout : endpoint.getTimeout();
    }

    private Map<String, Object> executeCopyOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Copy operation");
        
        String sourcePath = getParameterValue(exchange, "SAP_DropboxSourcePath", endpoint.getSourcePath());
        String destPath = getParameterValue(exchange, "SAP_DropboxDestinationPath", endpoint.getDestinationPath());
        String handling = getParameterValue(exchange, "SAP_DropboxHandling", endpoint.getHandlingExistingFiles());
        
        Map<String, Object> args = new HashMap<>();
        args.put("from_path", sourcePath);
        args.put("to_path", destPath);
        args.put("autorename", "AutoRename".equalsIgnoreCase(handling));
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/copy_v2", apiArgs);
    }

    private Map<String, Object> executeCreateFolderOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Create Folder operation");
        
        String folderPath = getParameterValue(exchange, "SAP_DropboxFolderPath", endpoint.getFolderPath());
        String handling = getParameterValue(exchange, "SAP_DropboxHandling", endpoint.getHandlingExistingFiles());
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", folderPath);
        args.put("autorename", "AutoRename".equalsIgnoreCase(handling));
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/create_folder_v2", apiArgs);
    }

    private Map<String, Object> executeDeleteOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Delete operation");
        
        String path = getParameterValue(exchange, "SAP_DropboxPath", endpoint.getFilePath());
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", path);
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/delete_v2", apiArgs);
    }

    private Map<String, Object> executeGetFileUrlOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Get File URL operation");
        
        String path = getParameterValue(exchange, "SAP_DropboxPath", endpoint.getFilePath());
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", path);
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/get_temporary_link", apiArgs);
    }

    private Map<String, Object> executeGetMetadataOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Get Metadata operation");
        
        String path = getParameterValue(exchange, "SAP_DropboxPath", endpoint.getFilePath());
        Boolean includeDeleted = getBooleanParameter(exchange, "SAP_DropboxIncludeDeleted", endpoint.isIncludeDeleted());
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", path);
        args.put("include_deleted", includeDeleted);
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/get_metadata", apiArgs);
    }

    private Map<String, Object> executeGetStorageStatisticsOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Get Storage Statistics operation");
        
        String apiArgs = "null";
        return client.executeApiCall("/users/get_space_usage", apiArgs);
    }

    private Map<String, Object> executeListFolderOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing List Folder operation");
        
        String path = getParameterValue(exchange, "SAP_DropboxPath", endpoint.getFolderPath());
        Boolean recursive = getBooleanParameter(exchange, "SAP_DropboxRecursive", endpoint.isRecursive());
        Integer limit = getIntegerParameter(exchange, "SAP_DropboxLimit", endpoint.getLimit());
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", path != null ? path : "");
        args.put("recursive", recursive);
        args.put("limit", limit);
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/list_folder", apiArgs);
    }

    private Map<String, Object> executeListRevisionsOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing List Revisions operation");
        
        String path = getParameterValue(exchange, "SAP_DropboxPath", endpoint.getFilePath());
        String mode = getParameterValue(exchange, "SAP_DropboxMode", endpoint.getMode());
        Integer limit = getIntegerParameter(exchange, "SAP_DropboxLimit", endpoint.getLimit());
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", path);
        args.put("mode", mode);
        args.put("limit", Math.min(limit, 100)); // Max 100 for revisions
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/list_revisions", apiArgs);
    }

    private Map<String, Object> executeMoveOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Move operation");
        
        String sourcePath = getParameterValue(exchange, "SAP_DropboxSourcePath", endpoint.getSourcePath());
        String destPath = getParameterValue(exchange, "SAP_DropboxDestinationPath", endpoint.getDestinationPath());
        String handling = getParameterValue(exchange, "SAP_DropboxHandling", endpoint.getHandlingExistingFiles());
        
        Map<String, Object> args = new HashMap<>();
        args.put("from_path", sourcePath);
        args.put("to_path", destPath);
        args.put("autorename", "AutoRename".equalsIgnoreCase(handling));
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/move_v2", apiArgs);
    }

    private Map<String, Object> executeUpdateMetadataOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Update Metadata operation");
        
        String path = getParameterValue(exchange, "SAP_DropboxPath", endpoint.getFilePath());
        String templateId = getParameterValue(exchange, "SAP_DropboxTemplateId", endpoint.getTemplateId());
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", path);
        args.put("template_id", templateId);
        
        // Add/update fields if provided
        if (endpoint.getAddOrUpdateFields() != null) {
            // Note: addOrUpdateFields should be passed as a JSON string
            args.put("add_or_update_fields", endpoint.getAddOrUpdateFields());
        }
        
        // Remove fields if provided
        if (endpoint.getRemoveFields() != null) {
            String[] fields = endpoint.getRemoveFields().split(",");
            args.put("remove_fields", fields);
        }
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/file_properties/properties/update", apiArgs);
    }

    private Map<String, Object> executeSearchOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Search operation");
        
        String query = getParameterValue(exchange, "SAP_DropboxQuery", endpoint.getQuery());
        String path = getParameterValue(exchange, "SAP_DropboxPath", endpoint.getFolderPath());
        Integer maxResults = getIntegerParameter(exchange, "SAP_DropboxMaxResults", endpoint.getMaxResults());
        String orderBy = getParameterValue(exchange, "SAP_DropboxOrderBy", endpoint.getOrderBy());
        
        Map<String, Object> args = new HashMap<>();
        args.put("query", query);
        if (path != null && !path.isEmpty()) {
            args.put("path", path);
        }
        args.put("max_results", Math.min(maxResults, 1000));
        
        Map<String, Object> options = new HashMap<>();
        options.put("order_by", "modifiedTime".equalsIgnoreCase(orderBy) ? "last_modified_time" : "relevance");
        args.put("options", options);
        
        String apiArgs = mapToJson(args);
        return client.executeApiCall("/files/search_v2", apiArgs);
    }

    private Map<String, Object> executeUploadOperation(DropboxClient client, Exchange exchange) throws Exception {
        LOG.info("Executing Upload operation");
        
        String path = getParameterValue(exchange, "SAP_DropboxPath", endpoint.getFilePath());
        String handling = getParameterValue(exchange, "SAP_DropboxAfterProc", endpoint.getHandlingExistingFiles());
        Boolean mute = getBooleanParameter(exchange, "SAP_DropboxMute", endpoint.isMute());
        
        // Get file content from exchange body
        byte[] content = exchange.getIn().getBody(byte[].class);
        if (content == null) {
            throw new IllegalArgumentException("Request body is empty for upload operation");
        }
        
        Map<String, Object> args = new HashMap<>();
        args.put("path", path);
        args.put("mode", getUploadMode(handling));
        args.put("autorename", "AutoRename".equalsIgnoreCase(handling));
        args.put("mute", mute);
        
        String apiArgs = mapToJson(args);
        return client.executeContentApiCall("/files/upload", apiArgs, content);
    }

    private String getUploadMode(String handling) {
        if ("Overwrite".equalsIgnoreCase(handling)) {
            return "overwrite";
        }
        return "add";
    }

    private void setResponseInExchange(Exchange exchange, Map<String, Object> result) throws Exception {
        String responseFormat = endpoint.getResponseFormat();
        
        if (result.containsKey("content")) {
            // For download operations, set binary content
            exchange.getIn().setBody(result.get("content"));
        } else if (result.containsKey("response")) {
            String response = (String) result.get("response");
            
            if ("XML".equalsIgnoreCase(responseFormat)) {
                // Convert JSON to XML if requested
                // For simplicity, we'll keep JSON for now
                // In production, implement proper JSON to XML conversion
                exchange.getIn().setBody(response);
            } else {
                exchange.getIn().setBody(response);
            }
        }
        
        // Set status code header
        exchange.getIn().setHeader("SAP_DropboxStatusCode", result.get("statusCode"));
    }

    private String getParameterValue(Exchange exchange, String headerName, String defaultValue) {
        String value = exchange.getIn().getHeader(headerName, String.class);
        return value != null ? value : defaultValue;
    }

    private Boolean getBooleanParameter(Exchange exchange, String headerName, Boolean defaultValue) {
        String value = exchange.getIn().getHeader(headerName, String.class);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    private Integer getIntegerParameter(Exchange exchange, String headerName, Integer defaultValue) {
        Integer value = exchange.getIn().getHeader(headerName, Integer.class);
        return value != null ? value : defaultValue;
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
            } else if (value instanceof String[]) {
                json.append("[");
                String[] arr = (String[]) value;
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) json.append(",");
                    json.append("\"").append(arr[i].replace("\"", "\\\"")).append("\"");
                }
                json.append("]");
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}

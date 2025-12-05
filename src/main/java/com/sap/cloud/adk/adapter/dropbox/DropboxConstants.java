package com.sap.cloud.adk.adapter.dropbox;

/**
 * Constants for Dropbox Adapter
 * Defines header names, operations, and API endpoints
 */
public final class DropboxConstants {
    
    // Dropbox API Base URL
    public static final String DROPBOX_API_BASE_URL = "https://api.dropboxapi.com/2";
    public static final String DROPBOX_CONTENT_BASE_URL = "https://content.dropboxapi.com/2";
    
    // Header Names
    public static final String HEADER_TIMEOUT = "SAP_DropboxTimeout";
    public static final String HEADER_HANDLING = "SAP_DropboxHandling";
    public static final String HEADER_INCLUDE_DELETED = "SAP_DropboxIncludeDeleted";
    public static final String HEADER_RECURSIVE = "SAP_DropboxRecursive";
    public static final String HEADER_LIMIT = "SAP_DropboxLimit";
    public static final String HEADER_ORDER_BY = "SAP_DropboxOrderBy";
    public static final String HEADER_MUTE = "SAP_DropboxMute";
    public static final String HEADER_AFTER_PROC = "SAP_DropboxAfterProc";
    
    // Post-processing response headers
    public static final String HEADER_ARCHIVE_RESPONSE = "SAP_Dropbox_Archive_Response";
    public static final String HEADER_DELETE_RESPONSE = "SAP_Dropbox_Delete_Response";
    public static final String HEADER_KEEPFILE_RESPONSE = "SAP_Dropbox_Keepfile_Response";
    
    // Sender Operations
    public static final String OPERATION_DOWNLOAD_FILE = "downloadFile";
    public static final String OPERATION_DOWNLOAD_ARCHIVE = "downloadArchive";
    
    // Receiver Operations
    public static final String OPERATION_COPY = "copy";
    public static final String OPERATION_CREATE_FOLDER = "createFolder";
    public static final String OPERATION_DELETE = "delete";
    public static final String OPERATION_GET_FILE_URL = "getFileUrl";
    public static final String OPERATION_GET_METADATA = "getMetadata";
    public static final String OPERATION_GET_STORAGE_STATS = "getStorageStats";
    public static final String OPERATION_LIST_FOLDER = "listFolder";
    public static final String OPERATION_LIST_REVISIONS = "listRevisions";
    public static final String OPERATION_MOVE = "move";
    public static final String OPERATION_UPDATE_METADATA = "updateMetadata";
    public static final String OPERATION_SEARCH = "search";
    public static final String OPERATION_UPLOAD = "upload";
    
    // Dropbox API Endpoints
    public static final String API_DOWNLOAD = "/files/download";
    public static final String API_DOWNLOAD_ZIP = "/files/download_zip";
    public static final String API_COPY = "/files/copy_v2";
    public static final String API_CREATE_FOLDER = "/files/create_folder_v2";
    public static final String API_DELETE = "/files/delete_v2";
    public static final String API_GET_TEMPORARY_LINK = "/files/get_temporary_link";
    public static final String API_GET_METADATA = "/files/get_metadata";
    public static final String API_GET_SPACE_USAGE = "/users/get_space_usage";
    public static final String API_LIST_FOLDER = "/files/list_folder";
    public static final String API_LIST_REVISIONS = "/files/list_revisions";
    public static final String API_MOVE = "/files/move_v2";
    public static final String API_PROPERTIES_ADD = "/file_properties/properties/add";
    public static final String API_PROPERTIES_UPDATE = "/file_properties/properties/update";
    public static final String API_PROPERTIES_REMOVE = "/file_properties/properties/remove";
    public static final String API_SEARCH = "/files/search_v2";
    public static final String API_UPLOAD = "/files/upload";
    
    // Handling Options
    public static final String HANDLING_AUTO_RENAME = "autoRename";
    public static final String HANDLING_FAIL = "fail";
    public static final String HANDLING_IGNORE = "ignore";
    public static final String HANDLING_OVERWRITE = "overwrite";
    public static final String HANDLING_DYNAMIC = "dynamic";
    
    // Post-processing Options
    public static final String POST_PROC_DELETE = "delete";
    public static final String POST_PROC_KEEP = "keep";
    public static final String POST_PROC_MOVE = "move";
    public static final String POST_PROC_IDEMPOTENT = "idempotent";
    
    // Response Formats
    public static final String FORMAT_JSON = "json";
    public static final String FORMAT_XML = "xml";
    
    // Order By Options
    public static final String ORDER_BY_RELEVANCE = "relevance";
    public static final String ORDER_BY_MODIFIED_TIME = "modifiedTime";
    
    // Mode Options
    public static final String MODE_PATH = "path";
    public static final String MODE_ID = "id";
    
    // HTTP Headers
    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_DROPBOX_API_ARG = "Dropbox-API-Arg";
    public static final String HTTP_HEADER_DROPBOX_API_PATH_ROOT = "Dropbox-API-Path-Root";
    
    // Content Types
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
    
    // Error Messages
    public static final String ERROR_INVALID_OPERATION = "Invalid operation specified";
    public static final String ERROR_MISSING_CREDENTIAL = "Credential alias is required";
    public static final String ERROR_MISSING_PATH = "Path parameter is required";
    public static final String ERROR_API_CALL_FAILED = "Dropbox API call failed";
    public static final String ERROR_POST_PROCESSING_FAILED = "Post-processing operation failed";
    public static final String ERROR_INVALID_RESPONSE_FORMAT = "Invalid response format";
    
    // Idempotent Repository
    public static final String IDEMPOTENT_REPO_NAME = "dropbox-idempotent-repo";
    
    private DropboxConstants() {
        // Private constructor to prevent instantiation
    }
}

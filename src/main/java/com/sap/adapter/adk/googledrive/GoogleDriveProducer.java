package com.sap.adapter.adk.googledrive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.securestore.SecureStoreService;
import com.sap.it.api.securestore.UserCredential;
import com.sap.it.api.securestore.exception.SecureStoreException;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Google Drive Adapter Producer for SAP CPI Integration Suite.
 * Handles receiver operations for Google Drive integration.
 */
public class GoogleDriveProducer extends DefaultProducer {
    
    private static final Logger LOG = LoggerFactory.getLogger(GoogleDriveProducer.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive";
    
    private final GoogleDriveEndpoint endpoint;

    public GoogleDriveProducer(GoogleDriveEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        LOG.info("Processing Google Drive operation: {}", endpoint.getOperation());
        
        try {
            Drive driveService = createDriveService();
            String operation = endpoint.getOperation();
            
            if (operation == null || operation.isEmpty()) {
                operation = "upload";
            }
            
            String result;
            switch (operation.toLowerCase()) {
                case "upload":
                    result = uploadFile(driveService, exchange);
                    break;
                case "download":
                    result = downloadFile(driveService, exchange);
                    break;
                case "list":
                    result = listFiles(driveService, exchange);
                    break;
                case "delete":
                    result = deleteFile(driveService, exchange);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operation: " + operation);
            }
            
            exchange.getIn().setBody(result);
            exchange.setProperty("GOOGLE_DRIVE_OPERATION_STATUS", "SUCCESS");
            LOG.info("Google Drive operation completed successfully");
            
        } catch (Exception e) {
            LOG.error("Error processing Google Drive operation: {}", e.getMessage(), e);
            exchange.setProperty("GOOGLE_DRIVE_OPERATION_STATUS", "FAILED");
            exchange.setProperty("GOOGLE_DRIVE_ERROR_MESSAGE", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a Google Drive service instance using credentials from SAP CPI Secure Store.
     */
    private Drive createDriveService() throws Exception {
        LOG.info("Creating Google Drive service");
        
        // Get credentials from SAP CPI Secure Store
        SecureStoreService secureStoreService = ITApiFactory.getService(SecureStoreService.class, null);
        UserCredential credential = secureStoreService.getUserCredential(endpoint.getCredentialName());
        
        if (credential == null) {
            throw new SecureStoreException("Credential not found: " + endpoint.getCredentialName());
        }
        
        // Get the service account key JSON from the password field
        String serviceAccountJson = new String(credential.getPassword(), StandardCharsets.UTF_8);
        
        // Create credentials from service account JSON
        InputStream credentialsStream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(DRIVE_SCOPE));
        
        // Build and return Drive service
        String appName = endpoint.getApplicationName() != null ? 
                endpoint.getApplicationName() : "SAP-CPI-Google-Drive-Adapter";
        
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(appName)
                .build();
    }

    /**
     * Uploads a file to Google Drive.
     */
    private String uploadFile(Drive driveService, Exchange exchange) throws Exception {
        LOG.info("Uploading file to Google Drive");
        
        String fileName = endpoint.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            fileName = exchange.getIn().getHeader("CamelFileName", String.class);
            if (fileName == null) {
                fileName = "uploaded_file_" + System.currentTimeMillis();
            }
        }
        
        String mimeType = endpoint.getMimeType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/octet-stream";
        }
        
        // Create file metadata
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        
        // Set parent folder if specified
        if (endpoint.getFolderId() != null && !endpoint.getFolderId().isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(endpoint.getFolderId()));
        }
        
        // Get file content from exchange body
        Object body = exchange.getIn().getBody();
        InputStream inputStream;
        
        if (body instanceof InputStream) {
            inputStream = (InputStream) body;
        } else if (body instanceof byte[]) {
            inputStream = new ByteArrayInputStream((byte[]) body);
        } else if (body instanceof String) {
            inputStream = new ByteArrayInputStream(((String) body).getBytes(StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("Unsupported body type: " + body.getClass().getName());
        }
        
        InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);
        
        // Upload file
        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, mimeType, createdTime, size")
                .execute();
        
        LOG.info("File uploaded successfully. File ID: {}", uploadedFile.getId());
        
        // Set file ID in exchange header for downstream processing
        exchange.getIn().setHeader("GoogleDriveFileId", uploadedFile.getId());
        
        return String.format("File uploaded successfully. ID: %s, Name: %s", 
                uploadedFile.getId(), uploadedFile.getName());
    }

    /**
     * Downloads a file from Google Drive.
     */
    private String downloadFile(Drive driveService, Exchange exchange) throws Exception {
        LOG.info("Downloading file from Google Drive");
        
        String fileId = exchange.getIn().getHeader("GoogleDriveFileId", String.class);
        if (fileId == null || fileId.isEmpty()) {
            throw new IllegalArgumentException("File ID is required for download operation");
        }
        
        // Get file metadata
        File file = driveService.files().get(fileId)
                .setFields("id, name, mimeType, size")
                .execute();
        
        // Download file content
        InputStream inputStream = driveService.files().get(fileId)
                .executeMediaAsInputStream();
        
        // Set the downloaded content as exchange body
        exchange.getIn().setBody(inputStream);
        exchange.getIn().setHeader("CamelFileName", file.getName());
        exchange.getIn().setHeader("GoogleDriveMimeType", file.getMimeType());
        
        LOG.info("File downloaded successfully. Name: {}", file.getName());
        
        return String.format("File downloaded successfully. Name: %s, Size: %s bytes", 
                file.getName(), file.getSize());
    }

    /**
     * Lists files in Google Drive.
     */
    private String listFiles(Drive driveService, Exchange exchange) throws Exception {
        LOG.info("Listing files from Google Drive");
        
        String folderId = endpoint.getFolderId();
        String query = folderId != null && !folderId.isEmpty() ? 
                String.format("'%s' in parents and trashed=false", folderId) : 
                "trashed=false";
        
        FileList result = driveService.files().list()
                .setQ(query)
                .setPageSize(100)
                .setFields("nextPageToken, files(id, name, mimeType, createdTime, modifiedTime, size)")
                .execute();
        
        List<File> files = result.getFiles();
        
        if (files == null || files.isEmpty()) {
            LOG.info("No files found");
            return "No files found";
        }
        
        StringBuilder fileList = new StringBuilder("Files found:\n");
        for (File file : files) {
            fileList.append(String.format("- %s (ID: %s, Type: %s)\n", 
                    file.getName(), file.getId(), file.getMimeType()));
        }
        
        LOG.info("Listed {} files", files.size());
        
        return fileList.toString();
    }

    /**
     * Deletes a file from Google Drive.
     */
    private String deleteFile(Drive driveService, Exchange exchange) throws Exception {
        LOG.info("Deleting file from Google Drive");
        
        String fileId = exchange.getIn().getHeader("GoogleDriveFileId", String.class);
        if (fileId == null || fileId.isEmpty()) {
            throw new IllegalArgumentException("File ID is required for delete operation");
        }
        
        // Get file name before deletion for logging
        File file = driveService.files().get(fileId)
                .setFields("name")
                .execute();
        
        // Delete the file
        driveService.files().delete(fileId).execute();
        
        LOG.info("File deleted successfully. Name: {}", file.getName());
        
        return String.format("File deleted successfully. Name: %s", file.getName());
    }
}

package com.sap.cloud.adk.adapter.dropbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Dropbox API Client
 * Handles all HTTP communication with Dropbox API v2
 */
public class DropboxClient {
    
    private static final Logger LOG = LoggerFactory.getLogger(DropboxClient.class);
    
    private final String accessToken;
    private final int timeout;
    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;
    
    public DropboxClient(String accessToken, int timeout) {
        this.accessToken = accessToken;
        this.timeout = timeout;
        this.jsonMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }
    
    /**
     * Execute a Dropbox API call with JSON request/response
     */
    public String executeApiCall(String endpoint, Map<String, Object> parameters) throws IOException {
        String url = DropboxConstants.DROPBOX_API_BASE_URL + endpoint;
        LOG.debug("Executing API call to: {}", url);
        
        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpPost httpPost = new HttpPost(url);
            
            // Set headers
            httpPost.setHeader(DropboxConstants.HTTP_HEADER_AUTHORIZATION, "Bearer " + accessToken);
            httpPost.setHeader(DropboxConstants.HTTP_HEADER_CONTENT_TYPE, DropboxConstants.CONTENT_TYPE_JSON);
            
            // Set request body
            if (parameters != null && !parameters.isEmpty()) {
                String jsonBody = jsonMapper.writeValueAsString(parameters);
                LOG.trace("Request body: {}", jsonBody);
                httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            }
            
            // Execute request
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            LOG.debug("Response status: {}", statusCode);
            LOG.trace("Response body: {}", responseBody);
            
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new IOException("Dropbox API call failed with status " + statusCode + ": " + responseBody);
            }
        }
    }
    
    /**
     * Execute a content download operation
     */
    public DownloadResult executeDownload(String endpoint, Map<String, Object> parameters) throws IOException {
        String url = DropboxConstants.DROPBOX_CONTENT_BASE_URL + endpoint;
        LOG.debug("Executing download from: {}", url);
        
        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpPost httpPost = new HttpPost(url);
            
            // Set headers
            httpPost.setHeader(DropboxConstants.HTTP_HEADER_AUTHORIZATION, "Bearer " + accessToken);
            
            if (parameters != null && !parameters.isEmpty()) {
                String apiArg = jsonMapper.writeValueAsString(parameters);
                httpPost.setHeader(DropboxConstants.HTTP_HEADER_DROPBOX_API_ARG, apiArg);
                LOG.trace("Dropbox-API-Arg: {}", apiArg);
            }
            
            // Execute request
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = response.getEntity();
                byte[] content = readInputStream(entity.getContent());
                
                // Get metadata from response header
                String metadataHeader = response.getFirstHeader("Dropbox-API-Result").getValue();
                
                DownloadResult result = new DownloadResult();
                result.setContent(content);
                result.setMetadata(metadataHeader);
                
                LOG.debug("Downloaded {} bytes", content.length);
                return result;
            } else {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new IOException("Download failed with status " + statusCode + ": " + errorBody);
            }
        }
    }
    
    /**
     * Execute a content upload operation
     */
    public String executeUpload(String endpoint, byte[] content, Map<String, Object> parameters) throws IOException {
        String url = DropboxConstants.DROPBOX_CONTENT_BASE_URL + endpoint;
        LOG.debug("Executing upload to: {}", url);
        
        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpPost httpPost = new HttpPost(url);
            
            // Set headers
            httpPost.setHeader(DropboxConstants.HTTP_HEADER_AUTHORIZATION, "Bearer " + accessToken);
            httpPost.setHeader(DropboxConstants.HTTP_HEADER_CONTENT_TYPE, DropboxConstants.CONTENT_TYPE_OCTET_STREAM);
            
            if (parameters != null && !parameters.isEmpty()) {
                String apiArg = jsonMapper.writeValueAsString(parameters);
                httpPost.setHeader(DropboxConstants.HTTP_HEADER_DROPBOX_API_ARG, apiArg);
                LOG.trace("Dropbox-API-Arg: {}", apiArg);
            }
            
            // Set request body
            httpPost.setEntity(new ByteArrayEntity(content));
            
            // Execute request
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            LOG.debug("Upload response status: {}", statusCode);
            
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new IOException("Upload failed with status " + statusCode + ": " + responseBody);
            }
        }
    }
    
    /**
     * Convert JSON response to XML format
     */
    public String convertJsonToXml(String jsonResponse) throws IOException {
        try {
            JsonNode jsonNode = jsonMapper.readTree(jsonResponse);
            return xmlMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            LOG.error("Failed to convert JSON to XML", e);
            throw new IOException("Failed to convert response to XML format", e);
        }
    }
    
    /**
     * Parse response headers based on configuration
     */
    public Map<String, String> parseResponseHeaders(HttpResponse response, String headerConfig) {
        Map<String, String> headers = new HashMap<>();
        
        if (headerConfig == null || headerConfig.trim().isEmpty()) {
            return headers;
        }
        
        String[] headerNames = headerConfig.split("\\|");
        for (String headerName : headerNames) {
            String trimmedName = headerName.trim();
            if (response.containsHeader(trimmedName)) {
                headers.put(trimmedName, response.getFirstHeader(trimmedName).getValue());
            }
        }
        
        return headers;
    }
    
    /**
     * Create HTTP client with timeout configuration
     */
    private CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setSocketTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .build();
        
        return HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();
    }
    
    /**
     * Read input stream to byte array
     */
    private byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        buffer.flush();
        return buffer.toByteArray();
    }
    
    /**
     * Result object for download operations
     */
    public static class DownloadResult {
        private byte[] content;
        private String metadata;
        
        public byte[] getContent() {
            return content;
        }
        
        public void setContent(byte[] content) {
            this.content = content;
        }
        
        public String getMetadata() {
            return metadata;
        }
        
        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }
    }
}

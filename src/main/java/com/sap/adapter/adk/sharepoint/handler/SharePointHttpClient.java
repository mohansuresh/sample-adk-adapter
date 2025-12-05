package com.sap.adapter.adk.sharepoint.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.adapter.adk.sharepoint.auth.OAuth2AuthenticationHandler;
import com.sap.adapter.adk.sharepoint.exception.SharePointException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP client for SharePoint API operations
 * Handles authentication, request execution, and response processing
 */
public class SharePointHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(SharePointHttpClient.class);
    private static final String GRAPH_API_BASE_URL = "https://graph.microsoft.com/v1.0";

    private OAuth2AuthenticationHandler authHandler;
    private String siteUrl;
    private int connectionTimeout;
    private int socketTimeout;
    private String proxyHost;
    private int proxyPort;
    private boolean traceEnabled;

    public SharePointHttpClient(OAuth2AuthenticationHandler authHandler, String siteUrl,
                                int connectionTimeout, int socketTimeout,
                                String proxyHost, int proxyPort, boolean traceEnabled) {
        this.authHandler = authHandler;
        this.siteUrl = siteUrl;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.traceEnabled = traceEnabled;
    }

    /**
     * Executes a GET request to SharePoint
     * @param endpoint The API endpoint
     * @param queryParams Query parameters
     * @return Response body as String
     */
    public String executeGet(String endpoint, Map<String, String> queryParams) throws SharePointException {
        String url = buildUrl(endpoint, queryParams);
        HttpGet httpGet = new HttpGet(url);
        return executeRequest(httpGet);
    }

    /**
     * Executes a POST request to SharePoint
     * @param endpoint The API endpoint
     * @param requestBody Request body
     * @return Response body as String
     */
    public String executePost(String endpoint, String requestBody) throws SharePointException {
        String url = buildUrl(endpoint, null);
        HttpPost httpPost = new HttpPost(url);
        
        try {
            if (requestBody != null && !requestBody.isEmpty()) {
                httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
                httpPost.setHeader("Content-Type", "application/json");
            }
        } catch (Exception e) {
            throw new SharePointException("Error setting request body", e);
        }
        
        return executeRequest(httpPost);
    }

    /**
     * Executes a PUT request to SharePoint
     * @param endpoint The API endpoint
     * @param requestBody Request body
     * @return Response body as String
     */
    public String executePut(String endpoint, String requestBody) throws SharePointException {
        String url = buildUrl(endpoint, null);
        HttpPut httpPut = new HttpPut(url);
        
        try {
            if (requestBody != null && !requestBody.isEmpty()) {
                httpPut.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
                httpPut.setHeader("Content-Type", "application/json");
            }
        } catch (Exception e) {
            throw new SharePointException("Error setting request body", e);
        }
        
        return executeRequest(httpPut);
    }

    /**
     * Executes a PATCH request to SharePoint
     * @param endpoint The API endpoint
     * @param requestBody Request body
     * @return Response body as String
     */
    public String executePatch(String endpoint, String requestBody) throws SharePointException {
        String url = buildUrl(endpoint, null);
        HttpPatch httpPatch = new HttpPatch(url);
        
        try {
            if (requestBody != null && !requestBody.isEmpty()) {
                httpPatch.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
                httpPatch.setHeader("Content-Type", "application/json");
            }
        } catch (Exception e) {
            throw new SharePointException("Error setting request body", e);
        }
        
        return executeRequest(httpPatch);
    }

    /**
     * Executes a DELETE request to SharePoint
     * @param endpoint The API endpoint
     * @return Response body as String
     */
    public String executeDelete(String endpoint) throws SharePointException {
        String url = buildUrl(endpoint, null);
        HttpDelete httpDelete = new HttpDelete(url);
        return executeRequest(httpDelete);
    }

    /**
     * Downloads file content from SharePoint
     * @param endpoint The API endpoint
     * @return File content as byte array
     */
    public byte[] downloadFile(String endpoint) throws SharePointException {
        String url = buildUrl(endpoint, null);
        HttpGet httpGet = new HttpGet(url);
        
        try (CloseableHttpClient httpClient = createHttpClient()) {
            addAuthenticationHeader(httpGet);
            
            if (traceEnabled) {
                LOG.info("Downloading file from: {}", url);
            }
            
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                InputStream inputStream = response.getEntity().getContent();
                return EntityUtils.toByteArray(response.getEntity());
            } else {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new SharePointException("File download failed. Status: " + statusCode + ", Response: " + errorBody);
            }
        } catch (IOException e) {
            throw new SharePointException("Error downloading file", e);
        }
    }

    /**
     * Uploads file content to SharePoint
     * @param endpoint The API endpoint
     * @param fileContent File content as byte array
     * @return Response body as String
     */
    public String uploadFile(String endpoint, byte[] fileContent) throws SharePointException {
        String url = buildUrl(endpoint, null);
        HttpPut httpPut = new HttpPut(url);
        
        try (CloseableHttpClient httpClient = createHttpClient()) {
            addAuthenticationHeader(httpPut);
            httpPut.setEntity(new ByteArrayEntity(fileContent));
            httpPut.setHeader("Content-Type", "application/octet-stream");
            
            if (traceEnabled) {
                LOG.info("Uploading file to: {}", url);
            }
            
            HttpResponse response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            if (statusCode >= 200 && statusCode < 300) {
                LOG.info("File uploaded successfully");
                return responseBody;
            } else {
                throw new SharePointException("File upload failed. Status: " + statusCode + ", Response: " + responseBody);
            }
        } catch (IOException e) {
            throw new SharePointException("Error uploading file", e);
        }
    }

    /**
     * Executes an HTTP request
     * @param request The HTTP request
     * @return Response body as String
     */
    private String executeRequest(HttpRequestBase request) throws SharePointException {
        try (CloseableHttpClient httpClient = createHttpClient()) {
            addAuthenticationHeader(request);
            request.setHeader("Accept", "application/json");
            
            if (traceEnabled) {
                LOG.info("Executing {} request to: {}", request.getMethod(), request.getURI());
            }
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            if (traceEnabled) {
                LOG.debug("Response status: {}, Body: {}", statusCode, responseBody);
            }
            
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new SharePointException("Request failed. Status: " + statusCode + ", Response: " + responseBody);
            }
        } catch (IOException e) {
            throw new SharePointException("Error executing request", e);
        }
    }

    /**
     * Adds authentication header to the request
     * @param request The HTTP request
     */
    private void addAuthenticationHeader(HttpRequestBase request) {
        String accessToken = authHandler.getAccessToken();
        request.setHeader("Authorization", "Bearer " + accessToken);
    }

    /**
     * Creates an HTTP client with configured timeouts and proxy
     * @return CloseableHttpClient
     */
    private CloseableHttpClient createHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        
        RequestConfig.Builder configBuilder = RequestConfig.custom()
            .setConnectTimeout(connectionTimeout)
            .setSocketTimeout(socketTimeout);
        
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            configBuilder.setProxy(proxy);
            LOG.debug("Using proxy: {}:{}", proxyHost, proxyPort);
        }
        
        builder.setDefaultRequestConfig(configBuilder.build());
        return builder.build();
    }

    /**
     * Builds the full URL with query parameters
     * @param endpoint The API endpoint
     * @param queryParams Query parameters
     * @return Full URL
     */
    private String buildUrl(String endpoint, Map<String, String> queryParams) {
        StringBuilder url = new StringBuilder();
        
        if (endpoint.startsWith("http")) {
            url.append(endpoint);
        } else {
            url.append(GRAPH_API_BASE_URL);
            if (!endpoint.startsWith("/")) {
                url.append("/");
            }
            url.append(endpoint);
        }
        
        if (queryParams != null && !queryParams.isEmpty()) {
            url.append("?");
            queryParams.forEach((key, value) -> {
                url.append(key).append("=").append(value).append("&");
            });
            url.setLength(url.length() - 1); // Remove trailing &
        }
        
        return url.toString();
    }
}

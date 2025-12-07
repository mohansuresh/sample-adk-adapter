package com.sap.it.custom.dropbox.client;

import com.sap.it.api.ITApiFactory;
import com.sap.it.api.securestore.SecureStoreService;
import com.sap.it.api.securestore.UserCredential;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Dropbox API Client for making HTTP requests to Dropbox API v2.
 * Handles OAuth2 authentication and API communication.
 * 
 * @author SAP CPI Custom Adapter
 * @version 1.0.0
 */
public class DropboxClient {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxClient.class);
    
    private static final String DROPBOX_API_BASE_URL = "https://api.dropboxapi.com/2";
    private static final String DROPBOX_CONTENT_BASE_URL = "https://content.dropboxapi.com/2";
    
    private final String accessToken;
    private final int timeout;

    /**
     * Constructor that retrieves OAuth2 credentials from SAP Secure Store.
     *
     * @param credentialName The credential alias name
     * @param timeout        The timeout in milliseconds
     * @throws Exception if credentials cannot be retrieved
     */
    public DropboxClient(String credentialName, int timeout) throws Exception {
        this.timeout = timeout;
        this.accessToken = retrieveAccessToken(credentialName);
        LOG.info("DropboxClient initialized with timeout: {} ms", timeout);
    }

    /**
     * Retrieves the OAuth2 access token from SAP Secure Store.
     *
     * @param credentialName The credential alias name
     * @return The access token
     * @throws Exception if credentials cannot be retrieved
     */
    private String retrieveAccessToken(String credentialName) throws Exception {
        try {
            SecureStoreService secureStoreService = ITApiFactory.getService(SecureStoreService.class, null);
            UserCredential credential = secureStoreService.getUserCredential(credentialName);
            
            if (credential == null) {
                throw new IllegalArgumentException("Credential not found: " + credentialName);
            }
            
            // For OAuth2, the password field contains the access token
            char[] passwordChars = credential.getPassword();
            if (passwordChars == null || passwordChars.length == 0) {
                throw new IllegalArgumentException("Access token is empty for credential: " + credentialName);
            }
            
            String token = new String(passwordChars);
            LOG.info("Successfully retrieved access token for credential: {}", credentialName);
            return token;
            
        } catch (Exception e) {
            LOG.error("Failed to retrieve credentials: {}", e.getMessage(), e);
            throw new Exception("Failed to retrieve Dropbox credentials: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a Dropbox API call.
     *
     * @param endpoint    The API endpoint (e.g., "/files/download")
     * @param apiArgs     The API arguments as JSON string
     * @param requestBody The request body (for content upload)
     * @param isContent   Whether this is a content API call
     * @return The response as a Map
     * @throws IOException if the API call fails
     */
    public Map<String, Object> executeApiCall(String endpoint, String apiArgs, byte[] requestBody, boolean isContent) throws IOException {
        String baseUrl = isContent ? DROPBOX_CONTENT_BASE_URL : DROPBOX_API_BASE_URL;
        String url = baseUrl + endpoint;
        
        LOG.info("Executing Dropbox API call: {}", url);
        LOG.debug("API Args: {}", apiArgs);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            
            // Set authorization header
            httpPost.setHeader("Authorization", "Bearer " + accessToken);
            
            if (isContent) {
                // For content API calls
                if (apiArgs != null && !apiArgs.isEmpty()) {
                    httpPost.setHeader("Dropbox-API-Arg", apiArgs);
                }
                
                if (requestBody != null && requestBody.length > 0) {
                    httpPost.setHeader("Content-Type", "application/octet-stream");
                    httpPost.setEntity(new ByteArrayEntity(requestBody));
                } else {
                    httpPost.setHeader("Content-Type", "text/plain; charset=utf-8");
                }
            } else {
                // For regular API calls
                httpPost.setHeader("Content-Type", "application/json");
                if (apiArgs != null && !apiArgs.isEmpty()) {
                    httpPost.setEntity(new StringEntity(apiArgs, StandardCharsets.UTF_8));
                }
            }
            
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            
            LOG.info("Dropbox API response status: {}", statusCode);
            
            Map<String, Object> result = new HashMap<>();
            result.put("statusCode", statusCode);
            
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                if (isContent && endpoint.contains("/download")) {
                    // For download operations, return the binary content
                    byte[] content = EntityUtils.toByteArray(entity);
                    result.put("content", content);
                    
                    // Get metadata from response header
                    String metadataHeader = response.getFirstHeader("Dropbox-API-Result").getValue();
                    if (metadataHeader != null) {
                        result.put("metadata", metadataHeader);
                    }
                } else {
                    // For other operations, return JSON response
                    String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    result.put("response", responseBody);
                }
            }
            
            if (statusCode >= 400) {
                String errorMsg = "Dropbox API error: " + statusCode;
                if (result.containsKey("response")) {
                    errorMsg += " - " + result.get("response");
                }
                LOG.error(errorMsg);
                throw new IOException(errorMsg);
            }
            
            return result;
            
        } catch (IOException e) {
            LOG.error("Failed to execute Dropbox API call: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Executes a regular API call (non-content).
     *
     * @param endpoint The API endpoint
     * @param apiArgs  The API arguments as JSON string
     * @return The response as a Map
     * @throws IOException if the API call fails
     */
    public Map<String, Object> executeApiCall(String endpoint, String apiArgs) throws IOException {
        return executeApiCall(endpoint, apiArgs, null, false);
    }

    /**
     * Executes a content API call.
     *
     * @param endpoint    The API endpoint
     * @param apiArgs     The API arguments as JSON string
     * @param requestBody The request body
     * @return The response as a Map
     * @throws IOException if the API call fails
     */
    public Map<String, Object> executeContentApiCall(String endpoint, String apiArgs, byte[] requestBody) throws IOException {
        return executeApiCall(endpoint, apiArgs, requestBody, true);
    }
}

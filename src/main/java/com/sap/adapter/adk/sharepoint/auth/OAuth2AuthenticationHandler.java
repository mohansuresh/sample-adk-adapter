package com.sap.adapter.adk.sharepoint.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.adapter.adk.sharepoint.exception.SharePointAuthenticationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Handles OAuth2 authentication for SharePoint
 * Supports both OAuth2 Authorization Code and Client Credentials flows
 */
public class OAuth2AuthenticationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2AuthenticationHandler.class);

    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String scope;
    private String accessToken;
    private long tokenExpiryTime;

    public OAuth2AuthenticationHandler(String clientId, String clientSecret, String tenantId, String scope) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenantId = tenantId;
        this.scope = scope != null ? scope : "https://graph.microsoft.com/.default";
    }

    /**
     * Gets a valid access token, refreshing if necessary
     * @return Valid access token
     * @throws SharePointAuthenticationException if authentication fails
     */
    public String getAccessToken() throws SharePointAuthenticationException {
        if (accessToken == null || isTokenExpired()) {
            LOG.info("Access token is null or expired, acquiring new token");
            acquireToken();
        }
        return accessToken;
    }

    /**
     * Acquires a new access token using OAuth2 Client Credentials flow
     * @throws SharePointAuthenticationException if token acquisition fails
     */
    private void acquireToken() throws SharePointAuthenticationException {
        String tokenEndpoint = String.format(
            "https://login.microsoftonline.com/%s/oauth2/v2.0/token",
            tenantId
        );

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(tokenEndpoint);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            String requestBody = String.format(
                "client_id=%s&scope=%s&client_secret=%s&grant_type=client_credentials",
                clientId, scope, clientSecret
            );

            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            LOG.debug("Requesting OAuth2 token from: {}", tokenEndpoint);
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode == 200) {
                parseTokenResponse(responseBody);
                LOG.info("Successfully acquired OAuth2 access token");
            } else {
                LOG.error("Failed to acquire token. Status: {}, Response: {}", statusCode, responseBody);
                throw new SharePointAuthenticationException(
                    "Failed to acquire OAuth2 token. Status: " + statusCode + ", Response: " + responseBody
                );
            }
        } catch (IOException e) {
            LOG.error("Error acquiring OAuth2 token", e);
            throw new SharePointAuthenticationException("Error acquiring OAuth2 token", e);
        }
    }

    /**
     * Parses the token response and extracts access token and expiry time
     * @param responseBody JSON response from token endpoint
     */
    private void parseTokenResponse(String responseBody) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        
        if (jsonObject.has("access_token")) {
            this.accessToken = jsonObject.get("access_token").getAsString();
            
            // Calculate token expiry time (default to 3600 seconds if not provided)
            int expiresIn = jsonObject.has("expires_in") 
                ? jsonObject.get("expires_in").getAsInt() 
                : 3600;
            
            // Set expiry time with 5 minute buffer
            this.tokenExpiryTime = System.currentTimeMillis() + ((expiresIn - 300) * 1000L);
            
            LOG.debug("Token will expire in {} seconds", expiresIn);
        } else {
            throw new SharePointAuthenticationException("No access_token in response: " + responseBody);
        }
    }

    /**
     * Checks if the current token is expired
     * @return true if token is expired or about to expire
     */
    private boolean isTokenExpired() {
        return System.currentTimeMillis() >= tokenExpiryTime;
    }

    /**
     * Clears the cached access token
     */
    public void clearToken() {
        this.accessToken = null;
        this.tokenExpiryTime = 0;
    }
}

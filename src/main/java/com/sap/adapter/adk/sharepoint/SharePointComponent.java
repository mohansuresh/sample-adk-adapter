package com.sap.adapter.adk.sharepoint;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * SharePoint Component for SAP CPI Integration Suite
 * Manages SharePoint adapter endpoints
 * Compatible with Apache Camel 3.14
 */
public class SharePointComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SharePointComponent.class);

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        LOG.info("Creating SharePoint endpoint with URI: {}", uri);
        LOG.debug("Remaining: {}, Parameters: {}", remaining, parameters);

        SharePointEndpoint endpoint = new SharePointEndpoint(uri, this);
        setProperties(endpoint, parameters);

        // Validate required parameters
        validateEndpointConfiguration(endpoint);

        return endpoint;
    }

    /**
     * Validates the endpoint configuration
     * @param endpoint The SharePoint endpoint to validate
     * @throws IllegalArgumentException if required parameters are missing
     */
    private void validateEndpointConfiguration(SharePointEndpoint endpoint) {
        if (endpoint.getSiteUrl() == null || endpoint.getSiteUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Site URL is required for SharePoint adapter");
        }

        if (endpoint.getAuthenticationType() == null || endpoint.getAuthenticationType().trim().isEmpty()) {
            throw new IllegalArgumentException("Authentication Type is required for SharePoint adapter");
        }

        // Validate authentication-specific parameters
        String authType = endpoint.getAuthenticationType();
        if ("OAuth2".equalsIgnoreCase(authType) || "OAuth2ClientCredentials".equalsIgnoreCase(authType)) {
            if (endpoint.getClientId() == null || endpoint.getClientId().trim().isEmpty()) {
                throw new IllegalArgumentException("Client ID is required for OAuth2 authentication");
            }
            if (endpoint.getClientSecret() == null || endpoint.getClientSecret().trim().isEmpty()) {
                throw new IllegalArgumentException("Client Secret is required for OAuth2 authentication");
            }
            if (endpoint.getTenantId() == null || endpoint.getTenantId().trim().isEmpty()) {
                throw new IllegalArgumentException("Tenant ID is required for OAuth2 authentication");
            }
        }

        LOG.info("SharePoint endpoint configuration validated successfully");
    }
}

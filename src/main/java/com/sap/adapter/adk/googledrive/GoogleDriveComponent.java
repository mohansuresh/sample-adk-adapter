package com.sap.adapter.adk.googledrive;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Google Drive Adapter Component for SAP CPI Integration Suite.
 * This component manages the lifecycle of Google Drive endpoints.
 */
public class GoogleDriveComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleDriveComponent.class);

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters) throws Exception {
        LOG.info("Creating Google Drive endpoint with URI: {}", uri);
        LOG.info("Remaining: {}, parameters: {}", remaining, parameters);
        
        final Endpoint endpoint = new GoogleDriveEndpoint(uri, this);
        setProperties(endpoint, parameters);
        
        return endpoint;
    }
}

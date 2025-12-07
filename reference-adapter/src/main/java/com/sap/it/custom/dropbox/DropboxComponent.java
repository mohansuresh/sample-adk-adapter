package com.sap.it.custom.dropbox;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Dropbox Component for SAP Cloud Platform Integration.
 * Manages the lifecycle of Dropbox adapter endpoints.
 * 
 * @author SAP CPI Custom Adapter
 * @version 1.0.0
 */
public class DropboxComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxComponent.class);

    /**
     * Creates a new Dropbox endpoint with the given URI and parameters.
     *
     * @param uri        The endpoint URI
     * @param remaining  The remaining part of the URI after the scheme
     * @param parameters The endpoint parameters
     * @return A configured DropboxEndpoint instance
     * @throws Exception if endpoint creation fails
     */
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        LOG.info("Creating Dropbox endpoint with URI: {}", uri);
        LOG.debug("Remaining: {}, Parameters: {}", remaining, parameters);
        
        DropboxEndpoint endpoint = new DropboxEndpoint(uri, this);
        setProperties(endpoint, parameters);
        
        LOG.info("Dropbox endpoint created successfully");
        return endpoint;
    }
}

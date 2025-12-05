package com.sap.cloud.adk.adapter.dropbox;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Dropbox Component for SAP Cloud Platform Integration
 * Manages the lifecycle of Dropbox endpoints
 */
public class DropboxComponent extends DefaultComponent {
    
    private static final Logger LOG = LoggerFactory.getLogger(DropboxComponent.class);

    public DropboxComponent() {
        super();
        LOG.info("Dropbox Component initialized");
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        LOG.debug("Creating Dropbox endpoint with URI: {}, remaining: {}", uri, remaining);
        
        DropboxEndpoint endpoint = new DropboxEndpoint(uri, this);
        endpoint.setOperation(remaining);
        
        // Set parameters from URI
        setProperties(endpoint, parameters);
        
        LOG.info("Dropbox endpoint created for operation: {}", remaining);
        return endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        LOG.info("Dropbox Component started");
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        LOG.info("Dropbox Component stopped");
    }
}

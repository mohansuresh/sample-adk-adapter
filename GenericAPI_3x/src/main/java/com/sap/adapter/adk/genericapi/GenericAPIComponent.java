package com.sap.adapter.adk.genericapi;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Represents the component that manages.
 */
public class GenericAPIComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(GenericAPIComponent.class);


    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters) throws Exception {
        LOG.info("Creating the end point with camel 3x: {}", uri);
        LOG.info("Remaining: {}, parameters: {}", remaining, parameters);
        final Endpoint endpoint = new GenericAPIEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}

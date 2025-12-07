package com.sap.adapter.adk.adkhttps;

import com.sap.adapter.adk.adkhttps.endpoint.EndpointUriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AdkHttpsConsumerRegistry {

    private static AdkHttpsConsumerRegistry registry;
    private static final Logger logger = LoggerFactory.getLogger(AdkHttpsConsumerRegistry.class);
    private final Map<String, String> uriMap;


    private AdkHttpsConsumerRegistry() {
        uriMap = new HashMap<>();
    }

    public static AdkHttpsConsumerRegistry getRegistry() {
        if (registry == null) {
            registry = new AdkHttpsConsumerRegistry();
        }
        return registry;
    }

    /**
     * Provides a map of iflow id Vs end point Urls configured in Http Adapter
     * This is extracted from the registry which gets populated during iflow deployment
     *
     * @return the map of
     */
    public Map<String, Set<String>> getMapOfIntegrationFlowVsEndpointUris() {
        return EndpointUriUtil.getMapOfIntegrationFlowVsEndpointUris(uriMap);
    }


    public void addToRegistry(final String uri, final String contextName) {
        uriMap.put(uri.toLowerCase(), contextName);
    }

    public void removeFromRegistry(final String uri) {
        uriMap.remove(uri.toLowerCase());
    }

    public String checkDuplicateAndGetDuplicateContext(final String uri) {
        final Set<String> uriSet = uriMap.keySet();
        for (String uris : uriSet) {
            logger.info("UriMap contains: {}", uris);
        }
        // Check complete url
        if (uriSet.contains(uri)) {
            return uriMap.get(uri);
        }
        logger.info("Current URI : {}", uri);
        return null;
    }

}

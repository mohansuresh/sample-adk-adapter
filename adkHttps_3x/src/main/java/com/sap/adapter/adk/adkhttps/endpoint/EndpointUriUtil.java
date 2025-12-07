package com.sap.adapter.adk.adkhttps.endpoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EndpointUriUtil {

    private static final String ADAPTER_ENDPOINT_PREFIX = "adkHttps";
    private static final String FORWARD_SLASH = "/";

    private EndpointUriUtil() {
        // should not be instantiated
    }

    /**
     * Constructs a map of integration flow id Vs entry point URLs from a map of endpoint URL vs integration flow id
     * @return a map of integration flow id Vs Set<endpointUri>
     */
    public static Map<String, Set<String>> getMapOfIntegrationFlowVsEndpointUris(Map<String, String> mapOfUriVsIntegrationFlowId) {
        Map<String, Set<String>> integrationFlowVsEndpointUris = new HashMap<>();
        for (Map.Entry<String, String> entry : mapOfUriVsIntegrationFlowId.entrySet()) {
            String endpointUriWithNoPrefix = entry.getKey();
            String integrationFlowId = entry.getValue();
            String endpointUri = getEndPointUri(endpointUriWithNoPrefix);
            linkUriToIntegrationFlowId(integrationFlowVsEndpointUris, integrationFlowId, endpointUri);
        }
        return integrationFlowVsEndpointUris;
    }

    private static void linkUriToIntegrationFlowId(Map<String, Set<String>> integrationFlowVsEndpointUris, String integrationFlowId,
                                                   String endpointUri) {
        if (integrationFlowVsEndpointUris.get(integrationFlowId) == null) {
            Set<String> endpointUris = new HashSet<>();
            endpointUris.add(endpointUri);
            integrationFlowVsEndpointUris.put(integrationFlowId, endpointUris);
        } else {
            integrationFlowVsEndpointUris.get(integrationFlowId).add(endpointUri);
        }
    }

    private static String getEndPointUri(String endpointUriWithNoPrefix) {
        return getUriRelativeToWorkerNode(endpointUriWithNoPrefix);
    }

    private static String getUriRelativeToWorkerNode(String endpointUriWithNoPrefix) {
        return FORWARD_SLASH + ADAPTER_ENDPOINT_PREFIX + endpointUriWithNoPrefix;
    }

}

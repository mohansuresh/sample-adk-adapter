package com.sap.adapter.adk.adkhttps.endpoint;

import com.sap.adapter.adk.adkhttps.AdkHttpsConsumerRegistry;
import com.sap.it.api.adapter.monitoring.AdapterEndpointInformation;
import com.sap.it.api.adapter.monitoring.AdapterEndpointInformationService;
import com.sap.it.api.adapter.monitoring.AdapterEndpointInstance;
import com.sap.it.api.adapter.monitoring.EndpointCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpSenderAdapterEndpointInfoService implements AdapterEndpointInformationService {

    private static final Logger logger = LoggerFactory.getLogger(HttpSenderAdapterEndpointInfoService.class);

    @Override
    public List<AdapterEndpointInformation> getAdapterEndpointInformation() {
        Map<String, Set<String>> mapOfIntegrationFlowVsEndpointUris = AdkHttpsConsumerRegistry.getRegistry()
                .getMapOfIntegrationFlowVsEndpointUris();
        logger.info("Map of endpoint uris: {}", mapOfIntegrationFlowVsEndpointUris);
        return getEndPointInfoForAllIntegrationFlows(mapOfIntegrationFlowVsEndpointUris);
    }

    @Override
    public List<AdapterEndpointInformation> getAdapterEndpointInformationByIFlow(String integrationFlowId) {
        Map<String, Set<String>> mapOfIntegrationFlowVsEndpointUris = AdkHttpsConsumerRegistry.getRegistry()
                .getMapOfIntegrationFlowVsEndpointUris();
        List<AdapterEndpointInformation> adapterEndpointInformation = new ArrayList<>();
        // check if at least one integration flow uses the adapter
        if (mapOfIntegrationFlowVsEndpointUris != null) {
            Set<String> endPointUris = mapOfIntegrationFlowVsEndpointUris.get(integrationFlowId);
            if (endPointUris != null && !endPointUris.isEmpty()) {
                // create end point info only if there is any http sender present in the integration flow
                adapterEndpointInformation.add(getAdapterEndPointInfoForIntegrationFlow(integrationFlowId, endPointUris));
            }
        }
        return adapterEndpointInformation;
    }

    private List<AdapterEndpointInformation> getEndPointInfoForAllIntegrationFlows(
            Map<String, Set<String>> mapOfIntegrationFlowVsEndpointUris) {
        List<AdapterEndpointInformation> adapterEndpointInformation = new ArrayList<>();
        if (mapOfIntegrationFlowVsEndpointUris != null) {
            for (Map.Entry<String, Set<String>> entry : mapOfIntegrationFlowVsEndpointUris.entrySet()) {
                Set<String> endpointUris = entry.getValue();
                // create end point info only if there is any http sender present in the integration flow
                if (endpointUris != null && !endpointUris.isEmpty()) {
                    AdapterEndpointInformation adapterEndPointInfoForIntegrationFlow = getAdapterEndPointInfoForIntegrationFlow(entry.getKey(), endpointUris);
                    logger.info("Added information: {}", adapterEndPointInfoForIntegrationFlow.getIntegrationFlowId());
                    adapterEndpointInformation.add(adapterEndPointInfoForIntegrationFlow);
                }
            }
        }
        return adapterEndpointInformation;
    }

    private AdapterEndpointInformation getAdapterEndPointInfoForIntegrationFlow(String integrationFlowId, Set<String> entryPointUris) {
        AdapterEndpointInformation endPointInfo = new AdapterEndpointInformation();
        endPointInfo.setIntegrationFlowId(integrationFlowId);
        endPointInfo.setAdapterEndpointInstances(getAdapterEndPointInstancesForIntegrationFlow(entryPointUris));
        return endPointInfo;
    }

    private List<AdapterEndpointInstance> getAdapterEndPointInstancesForIntegrationFlow(Set<String> entryPointUris) {
        List<AdapterEndpointInstance> adapterEndPointInstances = new ArrayList<>();
        for (String entryPointUri : entryPointUris) {
            AdapterEndpointInstance endpointInstance = new AdapterEndpointInstance(EndpointCategory.ENTRY_POINT,
                    entryPointUri);
            logger.info("Adding endpointInstance: {} for uri: {}", endpointInstance, entryPointUri);
            adapterEndPointInstances.add(endpointInstance);
        }
        return adapterEndPointInstances;
    }

}
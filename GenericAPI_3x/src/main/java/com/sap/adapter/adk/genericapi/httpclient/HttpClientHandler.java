package com.sap.adapter.adk.genericapi.httpclient;

import com.sap.it.api.ccs.adapter.CloudConnectorProperties;
import com.sap.it.api.ccs.adapter.exception.CloudConnectorPropertiesException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientHandler.class);
    private static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    public String callServer(String address, String locationId, CloudConnectorProperties cloudConnectorProperties) throws IOException, CloudConnectorPropertiesException {

        try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet httpGet = new HttpGet(address);
            if (locationId != null) {
                LOGGER.info("Retrieved cloud connector properties: host: {}, port: {}",
                        cloudConnectorProperties.getProxyHost(), cloudConnectorProperties.getProxyPort());
                LOGGER.info("Additional headers: {}", cloudConnectorProperties.getAdditionalHeaders());
                HttpHost proxy = new HttpHost(cloudConnectorProperties.getProxyHost(), cloudConnectorProperties.getProxyPort());
                RequestConfig config = RequestConfig.custom()
                        .setProxy(proxy)
                        .build();
                String value = cloudConnectorProperties.getAdditionalHeaders().get(PROXY_AUTHORIZATION);
                if (value != null) {
                    httpGet.setHeader(PROXY_AUTHORIZATION, value);
                }
                httpGet.setConfig(config);
                httpGet.setHeader("SAP-Connectivity-SCC-Location_ID", locationId);
            }
            HttpResponse response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.error("Response Received! status code: {} and reason: {}", statusCode, response.getStatusLine().getReasonPhrase());

            String responseBody = getString(response.getEntity().getContent());
            LOGGER.error("response: {}", responseBody);
            if (statusCode == 200) {
                return getResult(locationId, responseBody, "success");
            } else {
                return getResult(locationId, responseBody, "failure");
            }
        }
    }

    private String getResult(String locationId, String actualResponse, String ccResponse) {
        if(locationId != null) {
            return ccResponse;
        } else {
            return actualResponse;
        }
    }

    private String getString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
}

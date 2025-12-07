package com.sap.adapter.adk.genericapi;

import com.sap.adapter.adk.genericapi.factory.AdapterSocketFactory;
import com.sap.adapter.adk.genericapi.httpclient.HttpClientHandler;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ccs.adapter.CloudConnectorContext;
import com.sap.it.api.ccs.adapter.CloudConnectorProperties;
import com.sap.it.api.ccs.adapter.ConnectionType;
import com.sap.it.api.ccs.adapter.exception.CloudConnectorPropertiesException;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;

/**
 * The www.Sample.com producer.
 */
public class GenericAPIProducer extends DefaultProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericAPIProducer.class);
    private final GenericAPIEndpoint endpoint;

    public GenericAPIProducer(GenericAPIEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(final Exchange exchange) throws Exception {
        CloudConnectorContext context = new CloudConnectorContext();

        ConnectionType connectionType = getConnectionType();
        context.setConnectionType(connectionType);
        CloudConnectorProperties cloudConnectorProperties = ITApiFactory.getService(CloudConnectorProperties.class, context);

        String res;
        if (connectionType.equals(ConnectionType.HTTP)) {
            res = processHttpRequest(cloudConnectorProperties);
        } else {
            res = processTCPRequest(cloudConnectorProperties);
        }

        exchange.setProperty("ON_PREMISE_CONNECTION_STATUS", res);
        LOGGER.error("Got response: with camel 3x {}", res);
        exchange.getIn().setBody(res);
        LOGGER.error("GenericAPI completed");
    }

    private String processHttpRequest(CloudConnectorProperties cloudConnectorProperties)
            throws CloudConnectorPropertiesException, IOException {
        HttpClientHandler handler = new HttpClientHandler();
        return handler.callServer(endpoint.getEndpointUrl(), endpoint.getLocationId(), cloudConnectorProperties);
    }

    private String processTCPRequest(CloudConnectorProperties cloudConnectorProperties) {
        try (Socket socket = AdapterSocketFactory.getSocket(cloudConnectorProperties, endpoint.getLocationId())) {
            InetSocketAddress unresolvedSocketAddress = InetSocketAddress.createUnresolved(endpoint.getProxyHost(), Integer.parseInt(endpoint.getProxyPort()));
            socket.connect(unresolvedSocketAddress, 30000);

            try (DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                out.writeUTF("Test message from GenericAPI adapter. At: " + LocalDateTime.now());
            }
        } catch (Exception e) {
            LOGGER.error("Could not perform the socket call: " + e.getMessage(), e);
            return "TCP Connection failed! " + e.getMessage();
        }
        return "TCP Connection is Successful";
    }

    private ConnectionType getConnectionType() {
        return (endpoint.getConnectionType() != null && endpoint.getConnectionType().equals("tcp")) ?
                ConnectionType.TCP :
                ConnectionType.HTTP;
    }

}

package com.sap.adapter.adk.genericapi;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultPollingEndpoint;

/**
 * Represents a www.Sample.com Camel endpoint.
 */
@UriEndpoint(scheme = "sap-generic", syntax = "", title = "")
public class GenericAPIEndpoint extends DefaultPollingEndpoint {

    @UriParam
    private String greetingsMessage;
    
    @UriParam
    private String credential;

    private String endpointUrl;

    private String locationId;

    private String connectionType;

    private String proxyHost;

    private String proxyPort;

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getCredential() {
		return credential;
	}

	public void setCredential(String credential) {
		this.credential = credential;
	}

	public String getGreetingsMessage() {
		return greetingsMessage;
	}

	public void setGreetingsMessage(String greetingsMessage) {
		this.greetingsMessage = greetingsMessage;
	}

	public GenericAPIEndpoint() {
    }

    public GenericAPIEndpoint(final String endpointUri, final GenericAPIComponent component) {
        super(endpointUri, component);
    }


    public Producer createProducer() {
        return new GenericAPIProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        final GenericAPIConsumer consumer = new GenericAPIConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    public boolean isSingleton() {
        return true;
    }

}

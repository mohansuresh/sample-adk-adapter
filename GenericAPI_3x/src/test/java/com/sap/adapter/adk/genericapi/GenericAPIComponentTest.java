package com.sap.adapter.adk.genericapi;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Test;

public class GenericAPIComponentTest extends CamelTestSupport {

    public void testSample() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        // mock ITApiFactory.getService(CloudConnectorProperties.class, context) to make this test run
        assertMockEndpointsSatisfied();
        Exchange exchange = mock.getExchanges().get(0);
		String finalResultFromProducer = exchange.getIn().getBody(String.class);
        System.out.println(finalResultFromProducer);
        String expected = "HELLO WORLD2";
        Assert.assertTrue("Did not get expected result", finalResultFromProducer.contains(expected));
    }

    @Override
    protected CamelContext createCamelContext() {
        return new DefaultCamelContext();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("sap-generic://foo?greetingsMessage=Hello world1&credential=Test")
                  .to("sap-generic://bar?greetingsMessage=Hello world2&credential=Test2")
                  .to("mock:result");
            }
        };
    }
}

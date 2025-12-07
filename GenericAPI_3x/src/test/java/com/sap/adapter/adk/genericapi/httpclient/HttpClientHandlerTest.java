package com.sap.adapter.adk.genericapi.httpclient;

import com.sap.it.api.ccs.adapter.exception.CloudConnectorPropertiesException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author I061700 on 03-09-2021
 */
public class HttpClientHandlerTest {

    @Test
    public void testCallServer() throws IOException, CloudConnectorPropertiesException {
        HttpClientHandler httpClientHandler = new HttpClientHandler();
        String result = httpClientHandler.callServer("https://www.sap.com/sea/index.model.json", null, null);
        Assert.assertFalse(result.isEmpty());
    }

}
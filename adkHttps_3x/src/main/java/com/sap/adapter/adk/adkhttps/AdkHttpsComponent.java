/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sap.adapter.adk.adkhttps;

import com.sap.adapter.adk.adkhttps.exception.DuplicateConsumerException;
import org.apache.camel.Endpoint;
import org.apache.camel.component.servlet.ServletComponent;
import org.apache.camel.component.servlet.ServletConsumer;
import org.apache.camel.component.servlet.ServletEndpoint;
import org.apache.camel.http.common.HttpConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

/**
 * Represents the component that manages.
 */
public class AdkHttpsComponent extends ServletComponent { //NOSONAR //it has more than 7 parents

    private static final String SERVLET_NAME = "AdkHttpsConsumer";
    private static final Logger logger = LoggerFactory.getLogger(AdkHttpsComponent.class);
    private static final String SEPARATOR = "//";

    public AdkHttpsComponent() {
        new AdkHttpsComponent(ServletEndpoint.class);
    }

    public AdkHttpsComponent(Class<? extends ServletEndpoint> endpointClass) {
        super(endpointClass);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        logger.debug("connecting with uri {}", uri);
        AdkHttpsConsumerRegistry registry = AdkHttpsConsumerRegistry.getRegistry();

        //verify duplicate entry
        String duplicateContext;
        if (uri.contains("&")) {
            uri = uri.substring(0, uri.indexOf('&'));
        }
        String currentURI = getUriPart(uri);
        logger.info("Uri for endpoint : {}", currentURI);
        duplicateContext = registry.checkDuplicateAndGetDuplicateContext(currentURI.toLowerCase(Locale.ENGLISH));
        if (duplicateContext != null) {
            logger.error("Registry contains above uri.");
            throw new DuplicateConsumerException("Error occurred during starting bundle -'"
                    + getCamelContext().getName() + "' : Http Address '"
                    + "' already registered for another iflow '" + duplicateContext + "'");
        }
        return super.createEndpoint(uri, remaining, parameters);
    }

    @Override
    public void disconnect(HttpConsumer consumer) throws Exception {
        super.disconnect(consumer);
        AdkHttpsConsumerRegistry registry = AdkHttpsConsumerRegistry.getRegistry();
        String uri = getUriPart(consumer.getEndpoint().getHttpUri().toString());
        registry.removeFromRegistry(uri);
        logger.debug("Removing uri from registry : {} Registry: {}", uri, registry);

    }

    @Override
    public void connect(HttpConsumer consumer) throws Exception {
        logger.debug("Connecting to servlet");
        ServletConsumer sc = (ServletConsumer) consumer;
        sc.getEndpoint().setServletName(SERVLET_NAME);
        String uri = getUriPart(consumer.getEndpoint().getHttpUri().toString());
        logger.debug("uri to connect: {}", uri);
        super.connect(sc);
        AdkHttpsConsumerRegistry.getRegistry().addToRegistry(uri, getCamelContext().getName());
    }

    private String getUriPart(String uri) {
        final String separator = (uri.contains(SEPARATOR)) ? SEPARATOR : ":";
        int index = uri.lastIndexOf(separator) + 1;
        return uri.substring(index);

    }

}

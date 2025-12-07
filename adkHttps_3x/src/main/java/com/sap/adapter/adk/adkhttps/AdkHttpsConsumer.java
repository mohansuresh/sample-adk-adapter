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

import com.sap.adapter.adk.adkhttps.exception.HttpResponseException;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.exception.InvalidContextException;
import com.sap.it.api.keystore.KeystoreService;
import com.sap.it.api.keystore.exception.KeystoreException;
import com.sap.it.api.msglog.adapter.AdapterMessageLogFactory;
import com.sap.it.api.msglog.adapter.AdapterMessageLogWithStatus;
import com.sap.it.api.msglog.adapter.AdapterTraceMessage;
import com.sap.it.api.msglog.adapter.AdapterTraceMessageType;
import com.sap.it.api.securestore.SecureStoreService;
import com.sap.it.api.securestore.UserCredential;
import com.sap.it.api.securestore.exception.SecureStoreException;
import org.apache.camel.Exchange;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.http.common.HttpConsumer;
import org.apache.camel.http.common.HttpHelper;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The Sample.com consumer.
 */
public class AdkHttpsConsumer extends CamelHttpTransportServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AdkHttpsConsumer.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.error("Service: {}", request); //$NON-NLS-1$

        // Is there a consumer registered for the request.
        HttpConsumer consumer = getServletResolveConsumerStrategy().resolve(request, getConsumers());
        if (consumer == null) {
            logger.error("No consumer to service request {}", request); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
            return;
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        logger.debug("***************************************");
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.error("Header - {}: {}", headerName, request.getHeader(headerName));
        }
        logger.debug("***************************************");
        Exchange exchange = consumer.getEndpoint().createExchange();
        HttpHelper.setCharsetFromContentType(request.getContentType(), exchange);
        DefaultMessage defaultMessage = new DefaultMessage(new DefaultCamelContext());
        defaultMessage.setExchange(exchange);
        exchange.setIn(defaultMessage);

        // set context path as header
        String contextPath = consumer.getEndpoint().getPath();
        exchange.getIn().setHeader("CamelServletContextPath", contextPath); //$NON-NLS-1$

        String httpPath = (String) exchange.getIn().getHeader(Exchange.HTTP_PATH);
        // here we just remove the CamelServletContextPath part from the HTTP_PATH
        if (httpPath != null && httpPath.startsWith(contextPath)) {
            exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath.substring(contextPath.length()));
        }

        // get Adapter Message Log Factory
        AdapterMessageLogFactory msgLogFactory = (AdapterMessageLogFactory) consumer.getEndpoint().getCamelContext()
                .getRegistry().lookupByName(AdapterMessageLogFactory.class.getName());
        addPublicApiHeaders(exchange);
        try (AdapterMessageLogWithStatus msgLog = msgLogFactory.getMessageLogWithStatus(exchange,
                "ADK HTTPS Incoming Message", "ADKHttpsSender", exchange.getExchangeId())) {

            if (msgLog.isTraceActive()) {
                writeTraceMessage(msgLog);
            }

            try {
                // process the exchange
                consumer.getProcessor().process(exchange);
            } catch (Throwable e) { // NOSONAR
                logger.error("Error processing request", e);
                exchange.setException(e);
            }

            if (exchange.getException() != null) {
                Exception exception = (Exception) ((exchange.getException().getCause() == null)
                        ? exchange.getException()
                        : exchange.getException().getCause());
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("An internal server error occurred: ");
                errorMessage.append(exception.getMessage()).append(".\n");

                String mplId = (String) exchange.getProperty("SAP_MessageProcessingLogID");
                errorMessage.append("The MPL ID for the failed message is : ").append(mplId).append("\n");
                errorMessage.append("For more details please check tail LOG.");
                exchange.setException(new HttpResponseException(errorMessage.toString()));
            }

            String message = "Error during write of the response: ";
            try {
                consumer.getBinding().writeResponse(exchange, response);
            } catch (Exception e) {
                logger.error(message, e);
                ServletException servletException = new ServletException(message, e);
                exchange.setException(servletException);
                throw servletException;
            }

        }

    }

    private void addPublicApiHeaders(Exchange exchange) {
        try {
            checkSecureStoreService(exchange);
            checkKeyStoreService(exchange);
        } catch (InvalidContextException | SecureStoreException | KeystoreException e) {
            logger.error("Error processing request: {}", e.getMessage(), e);
        }
    }

    private void checkSecureStoreService(Exchange exchange) throws InvalidContextException, SecureStoreException {
        SecureStoreService secureStoreService = ITApiFactory.getService(SecureStoreService.class, null);
        UserCredential userCredential = secureStoreService.getUserCredential("APITest");
        if (userCredential != null) {
            char[] password = userCredential.getPassword();
            String passPhrase = new String(password);
            exchange.setProperty("ADKPassword", passPhrase);
            String username = userCredential.getUsername();
            exchange.setProperty("ADKUserName", username);
            Map<String, String> credentialProperties = userCredential.getCredentialProperties();
            logger.debug("credentialProperties: {}", credentialProperties);
        } else {
            logger.error("Error in Get UserCredential: {} not found", "APITest");
        }
    }

    private void checkKeyStoreService(Exchange exchange) throws InvalidContextException, KeystoreException {
        KeystoreService keystoreService = ITApiFactory.getService(KeystoreService.class, null);
        String privateKey = "picouser";
        Key key = keystoreService.getKey(privateKey);
        if (key == null) {
            logger.error("Error in keystoreService while getting key: {}", privateKey);
        } else {
            exchange.setProperty("ADKPrivateKeyAlgorithm", key.getAlgorithm());
        }
        Certificate certificate = keystoreService.getCertificate(privateKey);
        if (certificate == null) {
            logger.error("Error in keystoreService while getting certificate: {}", privateKey);
        } else {
            exchange.setProperty("ADKCertificateAlgorithm", certificate.getPublicKey().getAlgorithm());
        }
        KeyPair keyPair = keystoreService.getKeyPair(privateKey);
        if (keyPair == null) {
            logger.error("Error in keystoreService while getting keyPair: {}", privateKey);
        } else {
            exchange.setProperty("ADKKeyPairAlgorithm", keyPair.getPrivate().getAlgorithm());
        }
    }

    private void writeTraceMessage(final AdapterMessageLogWithStatus msgLog) {

        // create header
        Map<String, String> traceHeader = new HashMap<>();
        traceHeader.put("traceHeader1", "traceHeaderValue1");
        logger.debug("Entered trace blocked");

        // create payload
        byte[] payload = String.format("Trace Payload for type: %s", AdapterTraceMessageType.SENDER_INBOUND).getBytes(StandardCharsets.UTF_8);

		// create trace message to write to the msg LOG
        AdapterTraceMessage traceMsg = msgLog.createTraceMessage(AdapterTraceMessageType.SENDER_INBOUND, payload, false);
        traceMsg.setHeaders(traceHeader);
        msgLog.writeTrace(traceMsg);

        logger.debug("Trace written");
    }

}

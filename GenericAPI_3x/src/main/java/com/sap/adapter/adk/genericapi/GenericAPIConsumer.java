package com.sap.adapter.adk.genericapi;

import com.sap.adapter.adk.genericapi.exception.GenericAPIException;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.exception.InvalidContextException;
import com.sap.it.api.keystore.KeystoreService;
import com.sap.it.api.keystore.exception.KeystoreException;
import com.sap.it.api.securestore.SecureStoreService;
import com.sap.it.api.securestore.UserCredential;
import com.sap.it.api.securestore.exception.SecureStoreException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.ScheduledPollConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.Map;

/**
 * The Sample.com consumer.
 */
public class GenericAPIConsumer extends ScheduledPollConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(GenericAPIConsumer.class);

    private final GenericAPIEndpoint endpoint;

    public GenericAPIConsumer(final GenericAPIEndpoint endpoint, final Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected int poll() throws Exception {
        Exchange exchange = endpoint.createExchange();
        LOG.error("3.x camel exchange");
        String greetingsMessage = endpoint.getGreetingsMessage();
        if (greetingsMessage == null || greetingsMessage.isEmpty()) {
            LOG.error("The message is empty! Default one will be used");
            greetingsMessage = "Hello There!!";
        }

        String builder = greetingsMessage + " Now it is with camel3x " + new Date();
        exchange.getIn().setBody(builder);
        addPublicApiHeaders(exchange, endpoint);

        try {
            getProcessor().process(exchange);
            return 1;
        } finally {
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        }
    }

    private void addPublicApiHeaders(Exchange exchange, GenericAPIEndpoint endpoint) {
        try {
            checkSecureStoreService(exchange, endpoint);
            checkKeyStoreService();

        } catch (InvalidContextException | SecureStoreException | KeystoreException e) {
            LOG.error("Error processing request: {}", e.getMessage(), e);
        }
    }

    private void checkSecureStoreService(Exchange exchange, GenericAPIEndpoint endpoint)
            throws InvalidContextException, SecureStoreException {
        SecureStoreService secureStoreService = ITApiFactory.getService(SecureStoreService.class, null);
        String string = endpoint.getCredential();
        UserCredential userCredential = secureStoreService.getUserCredential(string);
        if (userCredential != null) {
            char[] password = userCredential.getPassword();
            String passphrase = new String(password);
            exchange.setProperty("ADKPassword", passphrase);
            String username = userCredential.getUsername();
            exchange.setProperty("ADKUserName", username);
            Map<String, String> credentialProperties = userCredential.getCredentialProperties();
            LOG.debug("credentialProperties: {}", credentialProperties);
        } else {
            LOG.error("Error in Get UserCredential: {} not found", string);
        }
    }

    private void checkKeyStoreService() throws InvalidContextException, KeystoreException {
        KeystoreService keystoreService = ITApiFactory.getService(KeystoreService.class, null);
        String privateKey = "picouser";
        Key key = keystoreService.getKey(privateKey);
        String errorFormat = "Error in keystoreService while getting %s: %s";
        if (key == null) {
            String error = String.format(errorFormat, "key", privateKey);
            LOG.error(error);
            throw new GenericAPIException(error);
        }
        Certificate certificate = keystoreService.getCertificate(privateKey);
        if (certificate == null) {
            String error = String.format(errorFormat, "certificate", privateKey);
            LOG.error(error);
            throw new GenericAPIException(error);
        }
        KeyPair keyPair = keystoreService.getKeyPair(privateKey);
        if (keyPair == null) {
            String error = String.format(errorFormat, "key pair", privateKey);
            LOG.error(error);
            throw new GenericAPIException(error);
        }
    }

}
